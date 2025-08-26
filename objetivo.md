# MMORPG [nome]

## Mobs
- **Spawns**
  - todo mob terá um spawn com base em um spawner (similar ao código do DR/WC).
- **Loot**
  - todo mob irá droppar até X crafting items de raridades diversas

Core → o que é o jogo. <br>
App → como as regras se combinam. <br>
Infra → como o JME e o hardware entregam isso (render, input, physics, assets). <br>
Bootstrap → start e wiring. <br>

fluxo padrão de states: <br>
initialize() <br>
onEnable() <br>
update() <br>
onDisable() <br>
cleanup() <br>

começo da execução <br>
MyGame.main() <br>
MyGame.simpleInitApp() <br>
GameModule.wire(this); // inicializa tudo. <br>
stateManager.attach() // muda a tela para LoadingState. <br>
LoadingState.Initialize() <br>
LoadingState.onEnable() <br>
getStateManager().attach(gameFactory.create()); // Muda para a tela GameState (ela contém GameFactory). <br>
getStateManager().detach(this); // Sai da tela atual. <br>
GameState.Initialize() <br>
GameState.onEnable() <br>

GameState -> tela que administra o jogo de fato, análogo ao Level1Screen do proj2.

NavigationState -> administrador de telas, aqui que é decidido qual tela chama, qual é chamada, como e quando.

com.mygame <br>
├─ core <br>
│   ├─ domain <br>
│   │   ├─ character (Character, Stats, Faction, LevelUpPolicy) <br>
│   │   ├─ item (Item, Weapon, Armor, Consumable, Rarity) <br>
│   │   ├─ quest (Quest, Objective, Dialogue, Choice) <br>
│   │   └─ world (TimeOfDay, Region, Encounter) <br>
│   └─ service (CombatService, QuestService, LootService, PathService) <br>
├─ app                          // “application layer” <br>
│   ├─ systems (CombatSystem, QuestSystem, TimeSystem, SaveSystem) <br>
│   └─ ports   (Renderer, Audio, Input, Physics, SaveGamePort, AssetsPort) <br>
├─ infra <br>
│   ├─ jme <br>
│   │   ├─ appstate (GameState, MenuState, HudState, DialogueState, LoadingState) <br>
│   │   ├─ control  (CharacterControl, NpcAIControl, ProjectileControl, DoorControl) <br>
│   │   ├─ factory  (WorldLoader, ModelFactory, AnimationFactory, AudioFactory) <br>
│   │   ├─ adapter  (JmeRenderer, JmeAudio, JmeInput, JmePhysics) <br>
│   │   └─ ui       (HudView, InventoryView, DialogueView) <br>
│   ├─ persistence (SaveGameRepositoryJson, AssetRepositoryImpl) <br>
│   └─ events      (EventBus, events: DamageEvent, DeathEvent, QuestUpdatedEvent) <br>
└─ bootstrap (MyGame extends SimpleApplication, DI/ServiceLocator, Config) <br>