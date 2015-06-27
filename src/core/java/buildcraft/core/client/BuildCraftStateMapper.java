package buildcraft.core.client;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.util.ResourceLocation;

import buildcraft.api.core.BCLog;
import buildcraft.energy.BuildCraftEnergy;

public class BuildCraftStateMapper extends StateMapperBase {
    public static final BuildCraftStateMapper INSTANCE = new BuildCraftStateMapper();

    public static String getPropertyString(IBlockState state) {
        return INSTANCE.getPropertyString(state.getProperties());
    }

    @Override
    protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
        if (state.getBlock() == BuildCraftEnergy.blockOil) {
            BCLog.logger.info("FFS ME! " + state);
        }
        ResourceLocation location = (ResourceLocation) Block.blockRegistry.getNameForObject(state.getBlock());
        location = new ResourceLocation(location.getResourceDomain().replace("|", ""), location.getResourcePath());
        return new ModelResourceLocation(location, getPropertyString(state));
    }
}
