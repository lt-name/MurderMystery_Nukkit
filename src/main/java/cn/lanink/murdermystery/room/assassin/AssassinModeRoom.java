package cn.lanink.murdermystery.room.assassin;

import cn.lanink.gamecore.utils.exception.RoomLoadException;
import cn.lanink.murdermystery.room.base.BaseRoom;
import cn.nukkit.level.Level;
import cn.nukkit.utils.Config;

/**
 * @author lt_name
 */
public class AssassinModeRoom extends BaseRoom {

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

    @Override
    protected void victoryReward(int victory) {

    }

}
