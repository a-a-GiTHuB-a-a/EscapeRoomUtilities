package com.daboxen.EscapeRoomUtilities;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;

import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(EscapeRoomUtilities.MODID)
public class EscapeRoomUtilities
{
        // Define mod id in a common place for everything to reference
        public static final String MODID = "escape_room_utilities";
        // Directly reference a slf4j logger
        private static final Logger LOGGER = LogUtils.getLogger();
        // Create a Deferred Register to hold Blocks which will all be registered under the "examplemod" namespace
        public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
        // Create a Deferred Register to hold Items which will all be registered under the "examplemod" namespace
        public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
        // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "examplemod" namespace
        public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

        // Creates a new Block with the id "examplemod:example_block", combining the namespace and path
        public static final DeferredBlock<Block> KILLBLOCK = BLOCKS.registerSimpleBlock("killblock", BlockBehaviour.Properties.of().mapColor(MapColor.STONE));
        // Creates a new BlockItem with the id "examplemod:example_block", combining the namespace and path
        public static final DeferredItem<BlockItem> KILLBLOCK_ITEM = ITEMS.registerSimpleBlockItem("killblock", KILLBLOCK);

        // Creates a new food item with the id "examplemod:example_id", nutrition 1 and saturation 2
        public static final DeferredItem<TimedItem> DEPLETED_ORB = ITEMS.registerItem(
                "depleted_heal_orb",
                (prop) -> {
                        return new TimedItem(prop, 400);
                },
                new Item.Properties().fireResistant()
        );
        public static final DeferredItem<Item> HEAL_ORB = ITEMS.registerSimpleItem("heal_orb", new Item.Properties().food(new FoodProperties.Builder()
                .alwaysEdible().nutrition(20).saturationModifier(20f).fast().usingConvertsTo(DEPLETED_ORB).build()).fireResistant());

        public static final DeferredRegister.DataComponents DATA_COMPONENTS = DeferredRegister.createDataComponents(MODID);

        public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TIME_TO_LIVE = DATA_COMPONENTS.registerComponentType(
                "time_to_live",
                builder -> builder
                        // The codec to read/write the data to disk
                        .persistent(Codec.intRange(0, Integer.MAX_VALUE))
                        // The codec to read/write the data across the network
                        .networkSynchronized(ByteBufCodecs.INT)
        );

        // Creates a creative tab with the id "examplemod:example_tab" for the example item, that is placed after the combat tab
        public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = CREATIVE_MODE_TABS.register("escape_room_items", () -> CreativeModeTab.builder()
                .title(Component.translatable("itemGroup.escape_room_items")) //The language key for the title of your CreativeModeTab
                .withTabsBefore(CreativeModeTabs.BUILDING_BLOCKS)
                .icon(() -> HEAL_ORB.get().getDefaultInstance())
                .displayItems((parameters, output) -> {
                        output.accept(Items.BEDROCK);
                        output.accept(KILLBLOCK.get());
                        output.accept(HEAL_ORB.get());
                }).build());

        // The constructor for the mod class is the first code that is run when your mod is loaded.
        // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
        public EscapeRoomUtilities(IEventBus modEventBus, ModContainer modContainer)
        {
                // Register the commonSetup method for modloading
                modEventBus.addListener(this::commonSetup);

                // Register the Deferred Register to the mod event bus so blocks get registered
                BLOCKS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so items get registered
                ITEMS.register(modEventBus);
                // Register the Deferred Register to the mod event bus so tabs get registered
                CREATIVE_MODE_TABS.register(modEventBus);

                // Register ourselves for server and other game events we are interested in.
                // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
                // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
                NeoForge.EVENT_BUS.register(this);

                // Register the item to a creative tab
                modEventBus.addListener(this::addCreative);

                // Register our mod's ModConfigSpec so that FML can create and load the config file for us
                modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        }

        private void commonSetup(final FMLCommonSetupEvent event)
        {
            // Some common setup code
                LOGGER.info("HELLO FROM COMMON SETUP");

                if (Config.logDirtBlock)
                        LOGGER.info("DIRT BLOCK >> {}", BuiltInRegistries.BLOCK.getKey(Blocks.DIRT));

                LOGGER.info(Config.magicNumberIntroduction + Config.magicNumber);

                Config.items.forEach((item) -> LOGGER.info("ITEM >> {}", item.toString()));
        }

        // Add the example block item to the building blocks tab
        private void addCreative(BuildCreativeModeTabContentsEvent event)
        {
                /*if (event.getTabKey() == CreativeModeTabs.BUILDING_BLOCKS)
                        event.accept(KILLBLOCK_ITEM);*/
        }

        // You can use SubscribeEvent and let the Event Bus discover methods to call
        @SubscribeEvent
        public void onServerStarting(ServerStartingEvent event)
        {
                // Do something when the server starts
                LOGGER.info("HELLO from server starting");
        }

        // You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
        @EventBusSubscriber(modid = MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
        public static class ClientModEvents
        {
                @SubscribeEvent
                public static void onClientSetup(FMLClientSetupEvent event)
                {
                        // Some client setup code
                        LOGGER.info("HELLO FROM CLIENT SETUP");
                        LOGGER.info("MINECRAFT NAME >> {}", Minecraft.getInstance().getUser().getName());
                }
        }
}