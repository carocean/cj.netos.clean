package cj.netos.clean.service;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.netos.clean.model.ContentItem;
import cj.netos.clean.model.ItemPointer;
import cj.netos.clean.model.TrafficPool;
import cj.studio.ecm.net.CircuitException;

import java.util.List;

public interface IPoolService {
    List<TrafficPool> pagePool(
            ICube cube,
            int limit,
            long offset
    )throws CircuitException;

    List<ContentItem> pageContentItem(ICube poolCube, long ctime, String limit, long itemSkip);

    void removeContentItem(ICube poolCube, ContentItem item);

    void cleanReceptorItem(ICube home, ItemPointer pointer);

    void cleanChannelItem(ICube channelCube, ItemPointer pointer);

}
