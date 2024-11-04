package toni.immersivetips.api;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import toni.immersivetips.foundation.ImmersiveTip;

import java.util.List;

public interface CollectTips {
    Event<CollectTips> EVENT = EventFactory.createArrayBacked(CollectTips.class, (listeners) -> (tips) -> {
        for (CollectTips event : listeners) {
            event.onCollectTips(tips);
        }
    });

    void onCollectTips(List<ImmersiveTip> tips);
}
