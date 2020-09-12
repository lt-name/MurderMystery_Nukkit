package cn.lanink.murdermystery.utils;

import cn.nukkit.utils.Config;

public class Language {

    //控制台
    public String scoreboardAPINotFound = "§c请安装计分板前置！";
    public String startLoadingAddons = "§e开始加载扩展...";
    public String addonsLoaded = "§e扩展加载完成！";
    public String swordSuccess = "§a Sword加载完成";
    public String swordFailure = "§cSword文件加载失败！请检查插件完整性！";
    public String defaultSkinSuccess = "§a 默认尸体皮肤加载完成";
    public String defaultSkinFailure = "§c 默认尸体皮肤加载失败！请检查插件完整性！";
    public String startLoadingRoom = "§e开始加载房间...";
    public String roomLoadedSuccess = "§a房间：%name% 已加载！";
    public String roomLoadedFailureByConfig = "§c房间：%name% 配置不完整，加载失败！";
    public String roomLoadedFailureByLevel = "§c房间：%name% 地图读取失败！";
    public String roomLoadedFailureByGameMode = "§c房间：%name% 游戏模式设置错误！没有找到游戏模式: %gameMode%";
    public String roomLevelBackup = "§a房间：%name% 未检测到地图备份，正在备份地图中...";
    public String roomLevelBackupExist = "§a房间：%name% 检测到地图备份！";
    public String roomLevelBackupNotExist = "§a房间：%name% 地图备份不存在！无法还原地图！";
    public String roomLevelRestoreLevelFailure = "§c房间：%name% 地图还原失败！";
    public String roomLoadedAllSuccess = "§e房间加载完成！当前已加载 %number% 个房间！";
    public String roomUnloadSuccess = "§c房间：%name% 已卸载！";
    public String roomUnloadFailure = "§c房间：%name% 非正常结束！";
    public String startLoadingSkin = "§e开始加载皮肤...";
    public String skinFailureByFormat = "§c %name% 加载失败，这可能不是一个正确的图片";
    public String skinFailureByName = "§c %name% 加载失败，请将皮肤文件命名为 skin.png";
    public String skinLoadedSuccess = "§a编号: %number% 皮肤: %name% 已加载";
    public String skinLoadedAllSuccess = "§e皮肤加载完成！当前已加载 %number% 个皮肤！";
    public String skinLoadedAllFailureByNumber = "§c当前皮肤数量小于16，部分玩家仍可使用自己的皮肤!";
    public String pluginEnable = "§e插件加载完成！欢迎使用！";
    public String pluginDisable = "§c插件卸载完成！";
    //命令
    public String useCmdInRoom = "§e >> §c游戏中无法使用其他命令";
    public String cmdHelp = "§a查看帮助：/%cmdName% help";
    public String userHelp = "§eMurderMystery--命令帮助 \n" +
            "§a/%cmdName% §e打开ui \n" +
            "§a/%cmdName% join §e加入随机游戏\n" +
            "§a/%cmdName% join mode:<游戏模式> §e加入指定模式游戏\n" +
            "§a/%cmdName% join <房间名称> §e加入游戏 \n" +
            "§a/%cmdName% joinspectator <房间名称> §e观战" +
            "§a/%cmdName% quit §e退出游戏 \n" +
            "§a/%cmdName% list §e查看房间列表";
    public String noPermission = "§c你没有权限！";
    public String joinRoom = "§a你已加入房间: %name%";
    public String joinRoomOnRoom = "§c你已经在一个房间中了!";
    public String joinRoomOnRiding = "§a请勿在骑乘状态下进入房间！";
    public String joinRandomRoom = "§a已为你随机分配房间！";
    public String joinRoomIsNeedInitialized = "§a房间初始化中，请稍后...";
    public String joinRoomIsPlaying = "§a该房间正在游戏中，请稍后...";
    public String joinRoomIsFull = "§a该房间已满人，请稍后...";
    public String joinRoomIsNotFound = "§a暂无符合条件的房间！";
    public String joinRoomNotAvailable = "§a暂无房间可用！";
    public String quitRoom = "§a你已退出房间";
    public String quitRoomNotInRoom = "§a你本来就不在游戏房间！";
    public String listRoom = "§e房间列表： §a %list%";
    public String useCmdInCon = "请在游戏内输入！";
    public String adminHelp = "§eMurderMystery--命令帮助 \n" +
            "§a/%cmdName% §e打开ui \n" +
            "§a/%cmdName% setroomname <名称> §e设置房间名称 \n" +
            "§a/%cmdName% setwaitspawn §e设置当前位置为游戏出生点 \n" +
            "§a/%cmdName% addrandomspawn  §e添加当前位置为游戏等待出生点 \n" +
            "§a/%cmdName% addgoldspawn §e添加当前位置为金锭生成点 \n" +
            "§a/%cmdName% setgoldspawntime <时间> §e设置金锭生成间隔 \n" +
            "§a/%cmdName% setwaittime <时间> §e设置游戏人数达到最少人数后的等待时间 \n" +
            "§a/%cmdName% setgametime <时间> §e设置每轮游戏最长时间 \n" +
            "§a/%cmdName% setgamemode <模式> §e设置房间游戏模式 \n" +
            "§a/%cmdName% setminplayers <人数> §e设置房间最少人数 \n" +
            "§a/%cmdName% setmaxplayers <人数> §e设置房间最多人数 \n" +
            "§a/%cmdName% startroom §e开始所在地图的房间游戏 \n" +
            "§a/%cmdName% stoproom §e强制关闭所在地图的房间 \n" +
            "§a/%cmdName% reloadroom §e重载所有房间 \n" +
            "§a/%cmdName% unloadroom §e关闭所有房间,并卸载配置";
    public String adminSetRoomName = "§a房间名字设置为: %roomName%";
    public String adminSetRoomNameExist = "§a已经有名为 \"%roomName%\" 的房间啦！";
    public String adminSetSpawn = "§a默认出生点设置成功！";
    public String adminAddRandomSpawn = "§a随机出生点添加成功！";
    public String adminAddGoldSpawn = "§a金锭生成点添加成功！";
    public String adminNotNumber = "§a输入的参数不是数字！";
    public String adminSetGoldSpawnTime = "§a金锭产出间隔已设置为： %time%";
    public String adminSetWaitTime = "§a等待时间已设置为：%time%";
    public String adminSetGameTime = "§a游戏时间已设置为：%time%";
    public String adminSetGameTimeShort = "§a游戏时间最小不能低于1分钟！";
    public String adminSetGameMode = "§a房间游戏模式已设置为: %roomMode%";
    public String adminSetGameModeNotFound = "§c房间游戏模式: %mode% 不存在！";
    public String adminSetMinPlayers = "§a房间最少人数已设置为: %minPlayers%";
    public String adminSetMaxPlayers = "§a房间最多人数已设置为: %maxPlayers%";
    public String adminStartRoom = "§a已强制开启游戏！";
    public String adminStartRoomNoPlayer = "§a房间人数不足%minPlayers%人,无法开始游戏！";
    public String adminStartRoomIsPlaying = "§c房间已经开始了！";
    public String adminLevelNoRoom = "§a当前地图不是游戏房间！";
    public String adminStopRoom = "§a已强制结束房间！";
    public String adminReload = "§a配置重载完成！请在后台查看信息！";
    public String adminUnload = "§a已卸载所有房间！请在后台查看信息！";
    public String playerChat = "§a[房间]§f %player%§b >>>§f %message%";
    public String playerDeathChat = "§c[死亡]§f %player%§b >>>§f %message%";
    public String playerSpectatorChat = "§a[旁观]§f %player%§b >>>§f %message%";
    public String tpJoinRoomLevel = "§e >> §c要进入游戏地图，请先加入游戏！";
    public String tpQuitRoomLevel = "§e >> §c退出房间请使用命令！";
    //道具
    public String itemDetectiveBow = "§e侦探之弓";
    public String itemDetectiveBowLore = "会自动补充消耗的箭\n提示:\n攻击队友,您也会死";
    public String itemKillerSword = "§c杀手之剑";
    public String itemKillerSwordLore = "杀手之剑\n提示:\n切换到杀手之剑时会获得短暂加速\n点击屏幕可使用飞剑";
    public String itemScan = "§c扫描器";
    public String itemScanLore = "扫描出所有存活的人的位置\n提示:\n使用后会显示所有平民/侦探的位置\n这将持续5秒\n(只有你能看见)";
    public String itemQuitRoom = "§c退出房间";
    public String itemQuitRoomLore = "手持点击,即可退出房间";
    public String itemPotion = "§e神秘药水";
    public String itemPotionLore = "未知效果的药水\n究竟是会带来好运，还是厄运？\n使用方法：直接饮用即可";
    public String itemShieldWall = "§a护盾生成器";
    public String itemShieldWallLore = "可以生成一面短时间存在的墙\n它的功能很差，但却能在关键时间救你一命\n使用方法：放在地面即可";
    public String itemSnowball = "§a减速雪球";
    public String itemSnowballLore = "命中后使目标减速2秒";
    public String useItemScan = "§a已显示所有玩家位置！";
    public String damageSnowball = "§a你被减速雪球打中了！";
    public String useItemSwordCD = "§a飞剑冷却中";
    public String useItemScanCD = "§a定位冷却中";
    public String exchangeItemsOnlyOne = "你只能携带一个 %name%";
    public String exchangeItem = "§a成功兑换到一个 %name%";
    public String exchangeUseGold = "§a需要使用金锭兑换 %name%";
    //房间模式
    public String Classic = "经典";
    public String Infected = "感染";
    //身份
    public String commonPeople = "平民";
    public String killer = "杀手";
    public String detective = "侦探";
    public String death = "死亡";
    public String spectator = "旁观";
    //游戏提示信息
    public String killPlayer = "§a你成功击杀了一位玩家！";
    public String killKiller = "§a你成功击杀了杀手！";
    public String deathTitle = "§c死亡";
    public String deathByKillerSubtitle = "§c你被杀手杀死了";
    public String deathByDamageTeammateSubtitle = "§c你击中了队友";
    public String deathByTeammateSubtitle = "§c你被队友打死了";
    public String killerDeathSubtitle = "§c你被击杀了";
    public String titleCommonPeopleTitle = "§a平民";
    public String titleCommonPeopleSubtitle = "活下去，就是胜利";
    public String titleDetectiveTitle = "§e侦探";
    public String titleDetectiveSubtitle = "找出杀手，并用弓箭击杀他";
    public String titleKillerTitle = "§c杀手";
    public String titleKillerSubtitle = "杀掉所有人";
    public String killerGetSwordTime = "§e杀手将在 %time% 秒后拿到剑！";
    public String killerGetSword = "§e杀手已拿到剑！";
    public String playerKilledByKiller = "§c一位§e%identity%§c被杀手杀死了！";
    public String commonPeopleBecomeDetective = "§a一位平民成为了新的侦探！";
    public String titleVictoryKillerTitle = "§a杀手获得胜利！";
    public String titleVictoryCommonPeopleSubtitle = "§a平民和侦探获得胜利！";
    public String victoryKillerBottom = "§e恭喜杀手获得胜利";
    public String victoryKillerScoreBoard = "§e恭喜杀手获得胜利! ";
    public String victoryCommonPeopleBottom = "§e恭喜平民和侦探获得胜利！";
    public String victoryCommonPeopleScoreBoard = "§e恭喜平民和侦探获得胜利！";
    public String scoreBoardTitle = "§e密室杀手";
    public String waitTimeScoreBoard = "房间模式: §a %roomMode% \n" +
            "玩家: §a %playerNumber%/%maxPlayers% \n" +
            "§a开始倒计时： §l§e %time%";
    public String waitScoreBoard = "房间模式: §a %roomMode% \n" +
            "玩家: §a %playerNumber%/%maxPlayers% \n" +
            "最低游戏人数为 %minPlayers% 人 \n 等待玩家加入中";
    public String waitTimeBottom = "§a当前已有: %playerNumber% 位玩家 \n §a游戏还有: %time% 秒开始！";
    public String waitBottom = "§c等待玩家加入中,当前已有: %playerNumber% 位玩家";
    public String gameTimeScoreBoard = "§l§a房间模式: §a %roomMode% \n" +
            "§l§a我的身份:§e %identity% \n" +
            "§l§a存活人数:§e %playerNumber% \n" +
            "§l§a剩余时间:§e %time% §a秒 ";
    public String detectiveSurvival = "§l§a侦探存活";
    public String detectiveDeath = "§l§c侦探死亡,弓已掉落!";
    public String gameEffectCDScoreBoard = "§l§a加速冷却:§e %time% §a秒 ";
    public String gameSwordCDScoreBoard = "§l§a飞剑冷却:§e %time% §a秒 ";
    public String gameScanCDScoreBoard = "§l§a扫描冷却:§e %time% §a秒 ";
    public String playerRespawnTime = "§c复活倒计时: %time%";
    //GUI相关
    public String userMenuButton1 = "§e随机加入房间";
    public String userMenuButton2 = "§e退出当前房间";
    public String userMenuButton3 = "§e查看房间列表";
    public String adminMenuSetLevel = "当前设置地图：%name%";
    public String adminMenuButton1 = "§e设置房间名称";
    public String adminMenuButton2 = "§e设置默认出生点";
    public String adminMenuButton3 = "§e添加随机出生点";
    public String adminMenuButton4 = "§e添加金锭生成点";
    public String adminMenuButton5 = "§e设置时间参数";
    public String adminMenuButton6 = "§e设置游戏人数";
    public String adminMenuButton7 = "§e设置房间模式";
    public String adminMenuButton8 = "§e重载所有房间";
    public String adminMenuButton9 = "§c卸载所有房间";
    public String adminTimeMenuInputText1 = "金锭产出间隔（秒）";
    public String adminTimeMenuInputText2 = "等待时间（秒）";
    public String adminTimeMenuInputText3 = "游戏时间（秒）";
    public String adminPlayersMenuInputText1 = "房间最少游戏人数";
    public String adminPlayersMenuInputText2 = "房间最多游戏人数";
    public String joinRoomOK = "§l§a确认要加入房间: %name% §l§a？";
    public String buttonSpectator = "§a观战";
    public String buttonOK = "§a确定";
    public String buttonReturn = "§c返回";


    public Language(Config config) {
        this.scoreboardAPINotFound = config.getString("scoreboardAPINotFound", this.scoreboardAPINotFound);
        this.startLoadingAddons = config.getString("startLoadingAddons", this.startLoadingAddons);
        this.addonsLoaded = config.getString("addonsLoaded", this.addonsLoaded);
        this.swordSuccess = config.getString("swordSuccess", this.swordSuccess);
        this.swordFailure = config.getString("swordFailure", this.swordFailure);
        this.defaultSkinSuccess = config.getString("defaultSkinSuccess", this.defaultSkinSuccess);
        this.defaultSkinFailure = config.getString("defaultSkinFailure", this.defaultSkinFailure);
        this.startLoadingRoom = config.getString("startLoadingRoom", this.startLoadingRoom);
        this.roomLoadedSuccess = config.getString("roomLoadedSuccess", this.roomLoadedSuccess);
        this.roomLoadedFailureByConfig = config.getString("roomLoadedFailureByConfig", this.roomLoadedFailureByConfig);
        this.roomLoadedFailureByLevel = config.getString("roomLoadedFailureByLevel", this.roomLoadedFailureByLevel);
        this.roomLoadedFailureByGameMode = config.getString("roomLoadedFailureByGameMode", this.roomLoadedFailureByGameMode);
        this.roomLevelBackup = config.getString("roomLevelBackup", this.roomLevelBackup);
        this.roomLevelBackupExist = config.getString("roomLevelBackupExist", this.roomLevelBackupExist);
        this.roomLevelBackupNotExist = config.getString("roomLevelBackupNotExist", this.roomLevelBackupNotExist);
        this.roomLevelRestoreLevelFailure = config.getString("roomLevelRestoreLevelFailure", this.roomLevelRestoreLevelFailure);
        this.roomLoadedAllSuccess = config.getString("roomLoadedAllSuccess", this.roomLoadedAllSuccess);
        this.roomUnloadSuccess = config.getString("roomUnloadSuccess", this.roomUnloadSuccess);
        this.roomUnloadFailure = config.getString("roomUnloadFailure", this.roomUnloadFailure);
        this.startLoadingSkin = config.getString("startLoadingSkin", this.startLoadingSkin);
        this.skinFailureByFormat = config.getString("skinFailureByFormat", this.skinFailureByFormat);
        this.skinFailureByName = config.getString("skinFailureByName", this.skinFailureByName);
        this.skinLoadedSuccess = config.getString("skinLoadedSuccess", this.skinLoadedSuccess);
        this.skinLoadedAllSuccess = config.getString("skinLoadedAllSuccess", this.skinLoadedAllSuccess);
        this.skinLoadedAllFailureByNumber = config.getString("skinLoadedAllFailureByNumber", this.skinLoadedAllFailureByNumber);
        this.pluginEnable = config.getString("pluginEnable", this.pluginEnable);
        this.pluginDisable = config.getString("pluginDisable", this.pluginDisable);
        this.useCmdInRoom = config.getString("useCmdInRoom", this.useCmdInRoom);
        this.cmdHelp = config.getString("cmdHelp", this.cmdHelp);
        this.userHelp = config.getString("userHelp", this.userHelp);
        this.noPermission = config.getString("noPermission", this.noPermission);
        this.joinRoom = config.getString("joinRoom", this.joinRoom);
        this.joinRoomOnRoom = config.getString("joinRoomOnRoom", this.joinRoomOnRoom);
        this.joinRoomOnRiding = config.getString("joinRoomOnRiding", this.joinRoomOnRiding);
        this.joinRandomRoom = config.getString("joinRandomRoom", this.joinRandomRoom);
        this.joinRoomIsNeedInitialized = config.getString("joinRoomIsNeedInitialized", this.joinRoomIsNeedInitialized);
        this.joinRoomIsPlaying = config.getString("joinRoomIsPlaying", this.joinRoomIsPlaying);
        this.joinRoomIsFull = config.getString("joinRoomIsFull", this.joinRoomIsFull);
        this.joinRoomIsNotFound = config.getString("joinRoomIsNotFound", this.joinRoomIsNotFound);
        this.joinRoomNotAvailable = config.getString("joinRoomNotAvailable", this.joinRoomNotAvailable);
        this.quitRoom = config.getString("quitRoom", this.quitRoom);
        this.quitRoomNotInRoom = config.getString("quitRoomNotInRoom", this.quitRoomNotInRoom);
        this.listRoom = config.getString("listRoom", this.listRoom);
        this.useCmdInCon = config.getString("useCmdInCon", this.useCmdInCon);
        this.adminHelp = config.getString("adminHelp", this.adminHelp);
        this.adminSetRoomName = config.getString("adminSetRoomName", this.adminSetRoomName);
        this.adminSetRoomNameExist = config.getString("adminSetRoomNameExist", this.adminSetRoomNameExist);
        this.adminSetSpawn = config.getString("adminSetSpawn", this.adminSetSpawn);
        this.adminAddRandomSpawn = config.getString("adminAddRandomSpawn", this.adminAddRandomSpawn);
        this.adminAddGoldSpawn = config.getString("adminAddGoldSpawn", this.adminAddGoldSpawn);
        this.adminNotNumber = config.getString("adminNotNumber", this.adminNotNumber);
        this.adminSetGoldSpawnTime = config.getString("adminSetGoldSpawnTime", this.adminSetGoldSpawnTime);
        this.adminSetWaitTime = config.getString("adminSetWaitTime", this.adminSetWaitTime);
        this.adminSetGameTime = config.getString("adminSetGameTime", this.adminSetGameTime);
        this.adminSetGameTimeShort = config.getString("adminSetGameTimeShort", this.adminSetGameTimeShort);
        this.adminSetGameMode = config.getString("adminSetGameMode", this.adminSetGameMode);
        this.adminSetGameModeNotFound = config.getString("adminSetGameModeNotFound", this.adminSetGameModeNotFound);
        this.adminSetMinPlayers = config.getString("adminSetMinPlayers", this.adminSetMinPlayers);
        this.adminSetMaxPlayers = config.getString("adminSetMaxPlayers", this.adminSetMaxPlayers);
        this.adminStartRoom = config.getString("adminStartRoom", this.adminStartRoom);
        this.adminStartRoomNoPlayer = config.getString("adminStartRoomNoPlayer", this.adminStartRoomNoPlayer);
        this.adminStartRoomIsPlaying = config.getString("adminStartRoomIsPlaying", this.adminStartRoomIsPlaying);
        this.adminLevelNoRoom = config.getString("adminLevelNoRoom", this.adminLevelNoRoom);
        this.adminStopRoom = config.getString("adminStopRoom", this.adminStopRoom);
        this.adminReload = config.getString("adminReload", this.adminReload);
        this.adminUnload = config.getString("adminUnload", this.adminUnload);
        this.playerChat = config.getString("playerChat", this.playerChat);
        this.playerDeathChat = config.getString("playerDeathChat", this.playerDeathChat);
        this.playerSpectatorChat = config.getString("playerSpectatorChat", this.playerSpectatorChat);
        this.tpJoinRoomLevel = config.getString("tpJoinRoomLevel", this.tpJoinRoomLevel);
        this.tpQuitRoomLevel = config.getString("tpQuitRoomLevel", this.tpQuitRoomLevel);
        this.itemDetectiveBow = config.getString("itemDetectiveBow", this.itemDetectiveBow);
        this.itemDetectiveBowLore = config.getString("itemDetectiveBowLore", this.itemDetectiveBowLore);
        this.itemKillerSword = config.getString("itemKillerSword", this.itemKillerSword);
        this.itemKillerSwordLore = config.getString("itemKillerSwordLore", this.itemKillerSwordLore);
        this.itemScan = config.getString("itemScan", this.itemScan);
        this.itemScanLore = config.getString("itemScanLore", this.itemScanLore);
        this.itemQuitRoom = config.getString("itemQuitRoom", this.itemQuitRoom);
        this.itemQuitRoomLore = config.getString("itemQuitRoomLore", this.itemQuitRoomLore);
        this.itemPotion = config.getString("itemPotion", this.itemPotion);
        this.itemPotionLore = config.getString("itemPotionLore", this.itemPotionLore);
        this.itemShieldWall = config.getString("itemShieldWall", this.itemShieldWall);
        this.itemShieldWallLore = config.getString("itemShieldWallLore", this.itemShieldWallLore);
        this.itemSnowball = config.getString("itemSnowball", this.itemSnowball);
        this.itemSnowballLore = config.getString("itemSnowballLore", this.itemSnowballLore);
        this.useItemScan = config.getString("useItemScan", this.useItemScan);
        this.damageSnowball = config.getString("damageSnowball", this.damageSnowball);
        this.useItemSwordCD = config.getString("useItemSwordCD", this.useItemSwordCD);
        this.useItemScanCD = config.getString("useItemScanCD", this.useItemScanCD);
        this.exchangeItemsOnlyOne = config.getString("exchangeItemsOnlyOne", this.exchangeItemsOnlyOne);
        this.exchangeItem = config.getString("exchangeItem", this.exchangeItem);
        this.exchangeUseGold = config.getString("exchangeUseGold", this.exchangeUseGold);
        this.Classic = config.getString("Classic", this.Classic);
        this.Infected = config.getString("Infected", this.Infected);
        this.commonPeople = config.getString("commonPeople", this.commonPeople);
        this.killer = config.getString("killer", this.killer);
        this.detective = config.getString("detective", this.detective);
        this.death = config.getString("death", this.death);
        this.spectator = config.getString("spectator", this.spectator);
        this.killPlayer = config.getString("killPlayer", this.killPlayer);
        this.killKiller = config.getString("killKiller", this.killKiller);
        this.deathTitle = config.getString("deathTitle", this.deathTitle);
        this.deathByKillerSubtitle = config.getString("deathByKillerSubtitle", this.deathByKillerSubtitle);
        this.deathByDamageTeammateSubtitle = config.getString("deathByDamageTeammateSubtitle", this.deathByDamageTeammateSubtitle);
        this.deathByTeammateSubtitle = config.getString("deathByTeammateSubtitle", this.deathByTeammateSubtitle);
        this.killerDeathSubtitle = config.getString("killerDeathSubtitle", this.killerDeathSubtitle);
        this.titleCommonPeopleTitle = config.getString("titleCommonPeopleTitle", this.titleCommonPeopleTitle);
        this.titleCommonPeopleSubtitle = config.getString("titleCommonPeopleSubtitle", this.titleCommonPeopleSubtitle);
        this.titleDetectiveTitle = config.getString("titleDetectiveTitle", this.titleDetectiveTitle);
        this.titleDetectiveSubtitle = config.getString("titleDetectiveSubtitle", this.titleDetectiveSubtitle);
        this.titleKillerTitle = config.getString("titleKillerTitle", this.titleKillerTitle);
        this.titleKillerSubtitle = config.getString("titleKillerSubtitle", this.titleKillerSubtitle);
        this.killerGetSwordTime = config.getString("killerGetSwordTime", this.killerGetSwordTime);
        this.killerGetSword = config.getString("killerGetSword", this.killerGetSword);
        this.playerKilledByKiller = config.getString("playerKilledByKiller", this.playerKilledByKiller);
        this.commonPeopleBecomeDetective = config.getString("commonPeopleBecomeDetective", this.commonPeopleBecomeDetective);
        this.titleVictoryKillerTitle = config.getString("titleVictoryKillerTitle", this.titleVictoryKillerTitle);
        this.titleVictoryCommonPeopleSubtitle = config.getString("titleVictoryCommonPeopleSubtitle", this.titleVictoryCommonPeopleSubtitle);
        this.victoryKillerBottom = config.getString("victoryKillerBottom", this.victoryKillerBottom);
        this.victoryKillerScoreBoard = config.getString("victoryKillerScoreBoard", this.victoryKillerScoreBoard);
        this.victoryCommonPeopleBottom = config.getString("victoryCommonPeopleBottom", this.victoryCommonPeopleBottom);
        this.victoryCommonPeopleScoreBoard = config.getString("victoryCommonPeopleScoreBoard", this.victoryCommonPeopleScoreBoard);
        this.scoreBoardTitle = config.getString("scoreBoardTitle", this.scoreBoardTitle);
        this.waitTimeScoreBoard = config.getString("waitTimeScoreBoard", this.waitTimeScoreBoard);
        this.waitScoreBoard = config.getString("waitScoreBoard", this.waitScoreBoard);
        this.waitTimeBottom = config.getString("waitTimeBottom", this.waitTimeBottom);
        this.waitBottom = config.getString("waitBottom", this.waitBottom);
        this.gameTimeScoreBoard = config.getString("gameTimeScoreBoard", this.gameTimeScoreBoard);
        this.detectiveSurvival = config.getString("detectiveSurvival", this.detectiveSurvival);
        this.detectiveDeath = config.getString("detectiveDeath", this.detectiveDeath);
        this.gameEffectCDScoreBoard = config.getString("gameEffectCDScoreBoard", this.gameEffectCDScoreBoard);
        this.gameSwordCDScoreBoard = config.getString("gameSwordCDScoreBoard", this.gameSwordCDScoreBoard);
        this.gameScanCDScoreBoard = config.getString("gameScanCDScoreBoard", this.gameScanCDScoreBoard);
        this.playerRespawnTime = config.getString("playerRespawnTime", this.playerRespawnTime);
        this.userMenuButton1 = config.getString("userMenuButton1", this.userMenuButton1);
        this.userMenuButton2 = config.getString("userMenuButton2", this.userMenuButton2);
        this.userMenuButton3 = config.getString("userMenuButton3", this.userMenuButton3);
        this.adminMenuSetLevel = config.getString("adminMenuSetLevel", this.adminMenuSetLevel);
        this.adminMenuButton1 = config.getString("adminMenuButton1", this.adminMenuButton1);
        this.adminMenuButton2 = config.getString("adminMenuButton2", this.adminMenuButton2);
        this.adminMenuButton3 = config.getString("adminMenuButton3", this.adminMenuButton3);
        this.adminMenuButton4 = config.getString("adminMenuButton4", this.adminMenuButton4);
        this.adminMenuButton5 = config.getString("adminMenuButton5", this.adminMenuButton5);
        this.adminMenuButton6 = config.getString("adminMenuButton6", this.adminMenuButton6);
        this.adminMenuButton7 = config.getString("adminMenuButton7", this.adminMenuButton7);
        this.adminMenuButton8 = config.getString("adminMenuButton8", this.adminMenuButton8);
        this.adminMenuButton9 = config.getString("adminMenuButton9", this.adminMenuButton9);
        this.adminTimeMenuInputText1 = config.getString("adminTimeMenuInputText1", this.adminTimeMenuInputText1);
        this.adminTimeMenuInputText2 = config.getString("adminTimeMenuInputText2", this.adminTimeMenuInputText2);
        this.adminTimeMenuInputText3 = config.getString("adminTimeMenuInputText3", this.adminTimeMenuInputText3);
        this.joinRoomOK = config.getString("joinRoomOK", this.joinRoomOK);
        this.buttonSpectator = config.getString("buttonSpectator", this.buttonSpectator);
        this.buttonOK = config.getString("buttonOK", this.buttonOK);
        this.buttonReturn = config.getString("buttonReturn", this.buttonReturn);
    }

}
