package net.dreamer.htl.block;

import net.dreamer.htl.HeartToLife;
import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.*;
import net.minecraft.block.pattern.BlockPattern;
import net.minecraft.block.pattern.BlockPatternBuilder;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.pathing.NavigationType;
import net.minecraft.predicate.block.BlockStatePredicate;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.function.Predicate;

public class SoulClusterBlock extends Block {
    @Nullable private BlockPattern sculkRemnantDispenserPattern;
    @Nullable private BlockPattern sculkRemnantPattern;
    @Nullable private BlockPattern soulGroundRemnantDispenserPattern;
    @Nullable private BlockPattern soulGroundRemnantPattern;
    private static final Predicate<BlockState> IS_SOUL_GROUND_PREDICATE;

    public SoulClusterBlock(Settings settings) {
        super(settings);
    }

    @SuppressWarnings("deprecated")
    public boolean isSideInvisible(BlockState state, BlockState stateFrom, Direction direction) {
        return stateFrom.isOf(this) || super.isSideInvisible(state,stateFrom,direction);
    }

    @SuppressWarnings("deprecated")
    public VoxelShape getCullingShape(BlockState state, BlockView world, BlockPos pos) {
        return VoxelShapes.empty();
    }

    @SuppressWarnings("deprecated")
    public boolean canPathfindThrough(BlockState state, BlockView world, BlockPos pos, NavigationType type) {
        return true;
    }

    @SuppressWarnings("deprecated")
    public VoxelShape getCollisionShape(BlockState state,BlockView world,BlockPos pos,ShapeContext context) {
        if (context instanceof EntityShapeContext entityShapeContext) {
            Entity entity = entityShapeContext.getEntity();
            if (entity != null) {
                BlockPos.Mutable blockOnFeet = new BlockPos.Mutable();
                blockOnFeet.set(entity.getBlockPos().getX(),entity.getBlockPos().getY(),entity.getBlockPos().getZ());
                BlockPos.Mutable blockOnHead = new BlockPos.Mutable();
                blockOnHead.set(entity.getBlockPos().getX(),entity.getBlockPos().getY() + 1,entity.getBlockPos().getZ());

                if (entity.isSneaking() || world.getBlockState(blockOnFeet).isOf(HeartToLife.SOUL_CLUSTER) && world.getBlockState(blockOnHead).isOf(HeartToLife.SOUL_CLUSTER)) return VoxelShapes.empty();
            }
        }

        return super.getCollisionShape(state, world, pos, context);
    }

    public void onLandedUpon(World world,BlockState state,BlockPos pos,Entity entity,float fallDistance) {
    }

    @SuppressWarnings("deprecated")
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        entity.slowMovement(state, new Vec3d(0.8D, 0.8D, 0.8D));
        if (!entity.isSneaking()) {
            Vec3d vec3d = entity.getVelocity();
            entity.setVelocity(vec3d.x, 0.2F, vec3d.z);
        }
    }

    public void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.isOf(state.getBlock())) this.trySpawnEntity(world, pos);
    }

    public boolean canDispense(WorldView world,BlockPos pos) {
        return this.getSculkRemnantDispenserPattern().searchAround(world, pos) != null || this.getSoulGroundRemnantDispenserPattern().searchAround(world, pos) != null;
    }

    private void trySpawnEntity(World world, BlockPos pos) {
        BlockPattern.Result result = this.getSculkRemnantPattern().searchAround(world, pos);
        int i;
        Iterator var6;
        ServerPlayerEntity serverPlayerEntity;
        int j;
        if (result != null) {
            for (i = 0; i < this.getSculkRemnantPattern().getHeight(); ++i) {
                CachedBlockPosition cachedBlockPosition = result.translate(0, i, 0);
                world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                world.syncWorldEvent(2001, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
            }

            ZombifiedRemnantEntity zombifiedRemnantEntity = HeartToLife.ZOMBIFIED_REMNANT.create(world);
            BlockPos blockPos = result.translate(0, 2, 0).getBlockPos();
            if (zombifiedRemnantEntity != null) {
                zombifiedRemnantEntity.refreshPositionAndAngles((double) blockPos.getX() + 0.5D,(double) blockPos.getY() + 0.05D,(double) blockPos.getZ() + 0.5D,0.0F,0.0F);
                zombifiedRemnantEntity.setRemnantType("sculk");
            }
            world.spawnEntity(zombifiedRemnantEntity);
            assert zombifiedRemnantEntity != null;
            var6 = world.getNonSpectatingEntities(ServerPlayerEntity.class, zombifiedRemnantEntity.getBoundingBox().expand(5.0D)).iterator();

            while (var6.hasNext()) {
                serverPlayerEntity = (ServerPlayerEntity)var6.next();
                if(zombifiedRemnantEntity.getOwner() == null) zombifiedRemnantEntity.setOwner(serverPlayerEntity);
                Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, zombifiedRemnantEntity);
            }

            for (j = 0; j < this.getSculkRemnantPattern().getHeight(); ++j) {
                CachedBlockPosition cachedBlockPosition2 = result.translate(0, j, 0);
                world.updateNeighbors(cachedBlockPosition2.getBlockPos(), Blocks.POWDER_SNOW);
            }
        } else {
            result = this.getSoulGroundRemnantPattern().searchAround(world, pos);
            if (result != null) {
                for (i = 0; i < this.getSoulGroundRemnantPattern().getHeight(); ++i) {
                    CachedBlockPosition cachedBlockPosition = result.translate(0, i, 0);
                    world.setBlockState(cachedBlockPosition.getBlockPos(), Blocks.AIR.getDefaultState(), 2);
                    world.syncWorldEvent(2001, cachedBlockPosition.getBlockPos(), Block.getRawIdFromState(cachedBlockPosition.getBlockState()));
                }

                ZombifiedRemnantEntity zombifiedRemnantEntity = HeartToLife.ZOMBIFIED_REMNANT.create(world);
                BlockPos blockPos = result.translate(0, 2, 0).getBlockPos();
                if (zombifiedRemnantEntity != null) {
                    zombifiedRemnantEntity.refreshPositionAndAngles((double) blockPos.getX() + 0.5D,(double) blockPos.getY() + 0.05D,(double) blockPos.getZ() + 0.5D,0.0F,0.0F);
                    zombifiedRemnantEntity.setRemnantType("soul_ground");
                }
                world.spawnEntity(zombifiedRemnantEntity);
                assert zombifiedRemnantEntity != null;
                var6 = world.getNonSpectatingEntities(ServerPlayerEntity.class, zombifiedRemnantEntity.getBoundingBox().expand(5.0D)).iterator();

                while (var6.hasNext()) {
                    serverPlayerEntity = (ServerPlayerEntity)var6.next();
                    if(zombifiedRemnantEntity.getOwner() == null) zombifiedRemnantEntity.setOwner(serverPlayerEntity);
                    Criteria.SUMMONED_ENTITY.trigger(serverPlayerEntity, zombifiedRemnantEntity);
                }

                for (j = 0; j < this.getSoulGroundRemnantPattern().getHeight(); ++j) {
                    CachedBlockPosition cachedBlockPosition2 = result.translate(0, j, 0);
                    world.updateNeighbors(cachedBlockPosition2.getBlockPos(), Blocks.POWDER_SNOW);
                }
            }
        }
    }

    private BlockPattern getSculkRemnantPattern() {
        if (this.sculkRemnantPattern == null) this.sculkRemnantPattern = BlockPatternBuilder.start().aisle("^","#","#").where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(HeartToLife.SOUL_CLUSTER))).where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SCULK))).build();

        return this.sculkRemnantPattern;
    }

    private BlockPattern getSculkRemnantDispenserPattern() {
        if (this.sculkRemnantDispenserPattern == null) this.sculkRemnantDispenserPattern = BlockPatternBuilder.start().aisle(" ","#","#").where('#', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(Blocks.SCULK))).build();

        return this.sculkRemnantDispenserPattern;
    }

    private BlockPattern getSoulGroundRemnantPattern() {
        if (this.soulGroundRemnantPattern == null) this.soulGroundRemnantPattern = BlockPatternBuilder.start().aisle("^","#","#").where('^', CachedBlockPosition.matchesBlockState(BlockStatePredicate.forBlock(HeartToLife.SOUL_CLUSTER))).where('#', CachedBlockPosition.matchesBlockState(IS_SOUL_GROUND_PREDICATE)).build();

        return this.soulGroundRemnantPattern;
    }

    private BlockPattern getSoulGroundRemnantDispenserPattern() {
        if (this.soulGroundRemnantDispenserPattern == null) this.soulGroundRemnantDispenserPattern = BlockPatternBuilder.start().aisle(" ","#","#").where('#', CachedBlockPosition.matchesBlockState(IS_SOUL_GROUND_PREDICATE)).build();

        return this.soulGroundRemnantDispenserPattern;
    }

    static {
        IS_SOUL_GROUND_PREDICATE = (state) -> state != null && (state.isIn(BlockTags.WITHER_SUMMON_BASE_BLOCKS) && !state.isOf(HeartToLife.SOUL_CLUSTER));
    }
}
