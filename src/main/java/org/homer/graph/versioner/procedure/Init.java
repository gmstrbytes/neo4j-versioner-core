package org.homer.graph.versioner.procedure;

import org.homer.graph.versioner.Utility;
import org.homer.graph.versioner.output.NodeOutput;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.procedure.*;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

/**
 * Init class, it contains all the Procedures needed to initialize an Entity node
 */
public class Init {

    @Context
    public GraphDatabaseService db;

    @Procedure(value = "graph.versioner.init", mode = Mode.WRITE)
    @Description("graph.versioner.init(entityLabel, {key:value,...}, {key:value,...}, additionalLabel, date) - Create an Entity node with an optional initial State.")
    public Stream<NodeOutput> init(
            @Name("entityLabel") String entityLabel,
            @Name(value = "entityProps", defaultValue = "{}") Map<String, Object> entityProps,
            @Name(value = "stateProps", defaultValue = "{}") Map<String, Object> stateProps,
            @Name(value = "additionalLabel", defaultValue = "") String additionalLabel,
            @Name(value = "date", defaultValue = "0") long date) {

        List<String> labelNames = new ArrayList<String>();
        labelNames.add(entityLabel);

        Node entity = Utility.setProperties(db.createNode(Utility.labels(labelNames)), entityProps);

        if (!stateProps.isEmpty()) {
            labelNames = new ArrayList<String>();
            labelNames.add(Utility.STATE_LABEL);
            if (!additionalLabel.isEmpty()) {
                labelNames.add(additionalLabel);
            }
            Node state = Utility.setProperties(db.createNode(Utility.labels(labelNames)), stateProps);

            long instantDate = (date == 0) ? Calendar.getInstance().getTimeInMillis() : date;
            entity.createRelationshipTo(state, RelationshipType.withName(Utility.CURRENT_TYPE)).setProperty(Utility.DATE_PROP, instantDate);
            entity.createRelationshipTo(state, RelationshipType.withName(Utility.HAS_STATE_TYPE)).setProperty(Utility.START_DATE_PROP, instantDate);
        }

        return Stream.of(new NodeOutput(entity));
    }
}
