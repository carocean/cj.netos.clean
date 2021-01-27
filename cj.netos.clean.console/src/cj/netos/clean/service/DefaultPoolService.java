package cj.netos.clean.service;

import cj.lns.chip.sos.cube.framework.ICube;
import cj.lns.chip.sos.cube.framework.IDocument;
import cj.lns.chip.sos.cube.framework.IQuery;
import cj.netos.clean.model.ContentItem;
import cj.netos.clean.model.ItemPointer;
import cj.netos.clean.model.TrafficPool;
import cj.studio.ecm.annotation.CjService;
import cj.studio.ecm.net.CircuitException;

import java.util.ArrayList;
import java.util.List;
@CjService(name = "poolService")
public class DefaultPoolService implements IPoolService {
    @Override
    public List<TrafficPool> pagePool(ICube cube, int limit, long offset) throws CircuitException {
        String cjql = String.format("select {'tuple':'*'}.limit(%s).skip(%s) from tuple %s %s where {}",limit,offset, TrafficPool._COL_NAME, TrafficPool.class.getName());
        IQuery<TrafficPool> query = cube.createQuery(cjql);
        List<IDocument<TrafficPool>> list=query.getResultList();
        List<TrafficPool> pools = new ArrayList<>();
        for (IDocument<TrafficPool> document : list) {
            pools.add(document.tuple());
        }
        return pools;
    }

    @Override
    public List<ContentItem> pageContentItem(ICube poolCube, long ctime, String limit, long offset) {
        String cjql = String.format("select {'tuple':'*'}.limit(%s).skip(%s).sort({'tuple.pointer.ctime':1}) from tuple %s %s where {'tuple.pointer.ctime':{'$lte':%s}}",
                limit, offset, ContentItem._COL_NAME, ContentItem.class.getName(), ctime);
        IQuery<ContentItem> query = poolCube.createQuery(cjql);
        List<IDocument<ContentItem>> list = query.getResultList();
        List<ContentItem> contentItems = new ArrayList<>();
        for (IDocument<ContentItem> document : list) {
            contentItems.add(document.tuple());
        }
        return contentItems;
    }

    @Override
    public void removeContentItem(ICube cube, ContentItem item) {
        cube.deleteDocOne("behavior.details",String.format("{'tuple.item':'%s'}",item.getId()));
        cube.deleteDocOne("behavior.innate",String.format("{'tuple.item':'%s'}",item.getId()));
        cube.deleteDocOne("behavior.inner",String.format("{'tuple.item':'%s'}",item.getId()));
        cube.deleteDocOne("content.items",String.format("{'tuple.id':'%s'}",item.getId()));
    }

    @Override
    public void cleanReceptorItem(ICube cube, ItemPointer pointer) {
        cube.deleteDocOne("geo.receptor.comments",String.format("{'tuple.docid':'%s'}",pointer.getId()));
        cube.deleteDocOne("geo.receptor.likes",String.format("{'tuple.docid':'%s'}",pointer.getId()));
        cube.deleteDocOne("geo.receptor.medias",String.format("{'tuple.docid':'%s'}",pointer.getId()));
        cube.deleteDocOne("geo.receptor.docs",String.format("{'tuple.id':'%s'}",pointer.getId()));
    }

    @Override
    public void cleanChannelItem(ICube cube, ItemPointer pointer) {
        cube.deleteDocOne("network.channel.documents.activities",String.format("{'tuple.docid':'%s'}",pointer.getId()));
        cube.deleteDocOne("network.channel.documents.comments",String.format("{'tuple.docid':'%s'}",pointer.getId()));
        cube.deleteDocOne("network.channel.documents.likes",String.format("{'tuple.docid':'%s'}",pointer.getId()));
        cube.deleteDocOne("network.channel.documents.medias",String.format("{'tuple.docid':'%s'}",pointer.getId()));
        cube.deleteDocOne("network.channel.documents",String.format("{'tuple.id':'%s'}",pointer.getId()));
    }
}
