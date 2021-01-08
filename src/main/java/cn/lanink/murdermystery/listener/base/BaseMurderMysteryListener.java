package cn.lanink.murdermystery.listener.base;

import cn.lanink.gamecore.listener.BaseGameListener;
import cn.lanink.murdermystery.MurderMystery;
import cn.lanink.murdermystery.room.base.BaseRoom;

/**
 * @author lt_name
 */
public abstract class BaseMurderMysteryListener<T extends BaseRoom> extends BaseGameListener<T> {

    protected final MurderMystery murderMystery = MurderMystery.getInstance();

}
