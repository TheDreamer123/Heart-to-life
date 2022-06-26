package net.dreamer.htl;

import net.dreamer.htl.block.SoulClusterBlock;
import net.dreamer.htl.entity.ZombifiedRemnantEntity;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.object.builder.v1.block.FabricBlockSettings;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricDefaultAttributeRegistry;
import net.fabricmc.fabric.api.object.builder.v1.entity.FabricEntityTypeBuilder;
import net.minecraft.block.*;
import net.minecraft.block.dispenser.FallibleItemDispenserBehavior;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HeartToLife implements ModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("Heart to Life");
	public static String MOD_ID = "htl";



	private static boolean never(BlockState state,BlockView world,BlockPos pos) {
		return false;
	}

	private static Boolean never(BlockState state, BlockView world, BlockPos pos, EntityType<?> type) {
		return false;
	}


	public static final Item WARDEN_HEART = new Item(new Item.Settings().group(ItemGroup.MISC).rarity(Rarity.RARE));

	public static final Block SOUL_CLUSTER = new SoulClusterBlock(FabricBlockSettings.copy(Blocks.SOUL_SAND).mapColor(MapColor.CYAN).sounds(BlockSoundGroup.SCULK).nonOpaque().velocityMultiplier(1.0F).allowsSpawning(HeartToLife::never).solidBlock(HeartToLife::never).suffocates(HeartToLife::never).blockVision(HeartToLife::never));


	public static SoundEvent REMNANT_DEATH = new SoundEvent(new Identifier(MOD_ID, "remnant.death"));
	public static SoundEvent REMNANT_HURT = new SoundEvent(new Identifier(MOD_ID, "remnant.hurt"));
	public static SoundEvent REMNANT_TALK = new SoundEvent(new Identifier(MOD_ID, "remnant.talk"));
	public static SoundEvent REMNANT_STEP = new SoundEvent(new Identifier(MOD_ID, "remnant.step"));

	public static final EntityType<ZombifiedRemnantEntity> ZOMBIFIED_REMNANT = Registry.register(Registry.ENTITY_TYPE,new Identifier(MOD_ID, "zombified_remnant"),FabricEntityTypeBuilder.create(SpawnGroup.MONSTER,(EntityType<ZombifiedRemnantEntity> entityType,World world) -> new ZombifiedRemnantEntity(world)).dimensions(EntityDimensions.fixed(0.6F, 1.95F)).build());

	@Override
	public void onInitialize() {
		LOGGER.info("Souls captured successfully. Have fun!!");



		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "warden_heart"), WARDEN_HEART);

		Registry.register(Registry.BLOCK, new Identifier(MOD_ID, "soul_cluster"), SOUL_CLUSTER);
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "soul_cluster"), new BlockItem(SOUL_CLUSTER, new Item.Settings().group(ItemGroup.BUILDING_BLOCKS)));

		DispenserBlock.registerBehavior(HeartToLife.SOUL_CLUSTER, new FallibleItemDispenserBehavior() {
			protected ItemStack dispenseSilently(BlockPointer pointer, ItemStack stack) {
				World world = pointer.getWorld();
				BlockPos blockPos = pointer.getPos().offset(pointer.getBlockState().get(DispenserBlock.FACING));
				SoulClusterBlock soulClusterBlock = (SoulClusterBlock)HeartToLife.SOUL_CLUSTER;
				if (world.isAir(blockPos) && soulClusterBlock.canDispense(world, blockPos)) {
					if (!world.isClient) {
						world.setBlockState(blockPos, soulClusterBlock.getDefaultState(), 3);
						world.emitGameEvent(null, GameEvent.BLOCK_PLACE, blockPos);
					}

					stack.decrement(1);
					this.setSuccess(true);
				}

				return stack;
			}
		});


		Registry.register(Registry.SOUND_EVENT, new Identifier(MOD_ID, "remnant.death"), REMNANT_DEATH);
		Registry.register(Registry.SOUND_EVENT, new Identifier(MOD_ID, "remnant.hurt"), REMNANT_HURT);
		Registry.register(Registry.SOUND_EVENT, new Identifier(MOD_ID, "remnant.talk"), REMNANT_TALK);
		Registry.register(Registry.SOUND_EVENT, new Identifier(MOD_ID, "remnant.step"), REMNANT_STEP);

		FabricDefaultAttributeRegistry.register(ZOMBIFIED_REMNANT, ZombifiedRemnantEntity.createMobAttributes().add(EntityAttributes.GENERIC_FOLLOW_RANGE, 35.0D).add(EntityAttributes.GENERIC_MOVEMENT_SPEED, 0.23000000417232513D).add(EntityAttributes.GENERIC_ATTACK_DAMAGE, 3.0D).add(EntityAttributes.GENERIC_ARMOR, 2.0D).add(EntityAttributes.ZOMBIE_SPAWN_REINFORCEMENTS));
	}
}
