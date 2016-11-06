package buildcraft.transport.pipe.behaviour;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.RayTraceResult;

import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;

import buildcraft.api.core.EnumPipePart;
import buildcraft.api.core.IStackFilter;
import buildcraft.api.transport.neptune.IFlowItems;
import buildcraft.api.transport.neptune.IItemPluggable;
import buildcraft.api.transport.neptune.IPipe;
import buildcraft.api.transport.neptune.PipeFlow;

import buildcraft.lib.inventory.filter.DelegatingItemHandlerFilter;
import buildcraft.lib.inventory.filter.InvertedStackFilter;
import buildcraft.lib.misc.EntityUtil;
import buildcraft.lib.misc.StackUtil;
import buildcraft.lib.tile.item.ItemHandlerSimple;
import buildcraft.transport.BCTransportGuis;

public class PipeBehaviourDiaWood extends PipeBehaviourWood {

    public enum FilterMode {
        WHITE_LIST,
        BLACK_LIST,
        ROUND_ROBIN;

        public static FilterMode get(int index) {
            switch (index) {
                default:
                case 0:
                    return WHITE_LIST;
                case 1:
                    return BLACK_LIST;
                case 2:
                    return ROUND_ROBIN;
            }
        }
    }

    public final ItemHandlerSimple filters = new ItemHandlerSimple(9, null);
    public FilterMode filterMode = FilterMode.WHITE_LIST;
    private int currentFilter = 0;

    public PipeBehaviourDiaWood(IPipe pipe) {
        super(pipe);
    }

    public PipeBehaviourDiaWood(IPipe pipe, NBTTagCompound nbt) {
        super(pipe, nbt);
        filters.deserializeNBT(nbt.getCompoundTag("filters"));
        filterMode = FilterMode.get(nbt.getByte("mode"));
        currentFilter = nbt.getByte("currentFilter") % filters.getSlots();
    }

    @Override
    public NBTTagCompound writeToNbt() {
        NBTTagCompound nbt = super.writeToNbt();
        nbt.setTag("filters", filters.serializeNBT());
        nbt.setByte("mode", (byte) filterMode.ordinal());
        nbt.setByte("currentFilter", (byte) currentFilter);
        return nbt;
    }

    @Override
    public void readPayload(PacketBuffer buffer, Side side, MessageContext ctx) {
        super.readPayload(buffer, side, ctx);
        if (side == Side.CLIENT) {
            filterMode = FilterMode.get(buffer.readUnsignedByte());
            currentFilter = buffer.readUnsignedByte() % filters.getSlots();
        }
    }

    @Override
    public void writePayload(PacketBuffer buffer, Side side) {
        super.writePayload(buffer, side);
        if (side == Side.SERVER) {
            buffer.writeByte(filterMode.ordinal());
            buffer.writeByte(currentFilter);
        }
    }

    @Override
    public boolean onPipeActivate(EntityPlayer player, RayTraceResult trace, float hitX, float hitY, float hitZ, EnumPipePart part) {
        if (EntityUtil.getWrenchHand(player) != null) {
            return super.onPipeActivate(player, trace, hitX, hitY, hitZ, part);
        }
        ItemStack held = player.getHeldItemMainhand();
        if (held != null) {
            if (held.getItem() instanceof IItemPluggable) {
                return false;
            }
        }
        if (!player.worldObj.isRemote) {
            BCTransportGuis.PIPE_DIAMOND_WOOD.openGUI(player, pipe.getHolder().getPipePos());
        }
        return true;
    }

    private IStackFilter getStackFilter() {
        switch (filterMode) {
            default:
            case WHITE_LIST:
                return new DelegatingItemHandlerFilter(StackUtil::isMatchingItemOrList, filters);
            case BLACK_LIST:
                return new InvertedStackFilter(new DelegatingItemHandlerFilter(StackUtil::isMatchingItemOrList, filters));
            case ROUND_ROBIN:
                return (comparison) -> {
                    ItemStack filter = filters.getStackInSlot(currentFilter);
                    return StackUtil.isMatchingItemOrList(filter, comparison);
                };
        }
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        // TODO: Make this require more or less than 1 Mj Per item
        // Also make this extract different numbers of items depending
        // on how much power was put in

        PipeFlow flow = pipe.getFlow();
        if (flow instanceof IFlowItems) {
            IStackFilter filter = getStackFilter();
            int extracted = ((IFlowItems) flow).tryExtractItems(1, getCurrentDir(), filter);
            if (extracted > 0 & filterMode == FilterMode.ROUND_ROBIN) {
                currentFilter++;
                if (currentFilter >= filters.getSlots()) {
                    currentFilter = 0;
                }
            }
        }
        return 0;
    }
}
