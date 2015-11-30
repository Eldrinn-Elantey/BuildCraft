package buildcraft.transport.pipes.bc8;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import buildcraft.api.transport.pipe_bc8.IPipeHolder_BC8;
import buildcraft.api.transport.pipe_bc8.IPipe_BC8;
import buildcraft.api.transport.pipe_bc8.PipeAPI_BC8;
import buildcraft.api.transport.pipe_bc8.PipeDefinition_BC8;
import buildcraft.core.lib.block.TileBuildCraft;

public class TilePipe_BC8 extends TileBuildCraft implements IPipeHolder_BC8 {
    private Pipe_BC8 pipe;

    public TilePipe_BC8() {

    }

    @Override
    public void onBlockPlacedBy(EntityLivingBase entity, ItemStack stack) {
        super.onBlockPlacedBy(entity, stack);
        PipeDefinition_BC8 def = PipeAPI_BC8.PIPE_REGISTRY.getDefinition(stack.getItem());
        pipe = new Pipe_BC8(this, def, getWorld());
    }

    @Override
    public IPipe_BC8 getPipe() {
        return pipe;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        String type = nbt.getString("type");
        PipeDefinition_BC8 definition = PipeAPI_BC8.PIPE_REGISTRY.getDefinition(type);
        pipe = new Pipe_BC8(this, definition, getWorld());
        pipe = pipe.readFromNBT(nbt.getTag("pipe"));
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setString("type", PipeAPI_BC8.PIPE_REGISTRY.getTag(pipe.getBehaviour().definition));
        nbt.setTag("pipe", pipe.writeToNBT());
    }

    @Override
    public void initialize() {
        pipe.initialize();
    }

}
