package cn.lanink.murdermystery.room.assassin;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.Player;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

import java.util.HashMap;

/**
 * @author lt_name
 */
public class AssassinModeRoom extends BaseRoom {

    public HashMap<Player, Player> targetMap= new HashMap<>(); //玩家-目标玩家

    /**
     * 初始化
     *
     * @param level  世界
     * @param config 配置文件
     */
    public AssassinModeRoom(Level level, Config config) throws RoomLoadException {
        super(level, config);
        //TODO 要实现此玩法，可能需要提前准备128x128的通缉令图片



    }

    /**
     * 启用监听器
     */
    @SuppressWarnings("unchecked")
    public void enableListener() {
        this.murderMystery.getMurderMysteryListeners().get("RoomLevelProtection").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultGameListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("DefaultChatListener").addListenerRoom(this);
        this.murderMystery.getMurderMysteryListeners().get("AssassinDamageListener").addListenerRoom(this);
    }

    @Override
    protected void victoryReward(int victory) {

    }

}
