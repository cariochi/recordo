package com.cariochi.recordo.mockmvc.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.springframework.boot.jackson.JsonComponent;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.domain.Sort.Order;

import java.io.IOException;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

@JsonComponent
public class SortJsonDeserializer extends JsonDeserializer<Sort> {

    @Override
    public Sort deserialize(JsonParser parser, DeserializationContext deserializationContext) throws IOException {
        final TreeNode treeNode = parser.getCodec().readTree(parser);
        return Optional.of(treeNode)
                .filter(TreeNode::isArray)
                .map(ArrayNode.class::cast)
                .map(this::deserializeOrders)
                .orElseGet(Sort::unsorted);
    }

    private Sort deserializeOrders(ArrayNode arrayNode) {
        return Sort.by(
                stream(arrayNode.spliterator(), false)
                        .map(this::deserializeOrder)
                        .collect(toList())
        );
    }

    private Order deserializeOrder(JsonNode node) {
        String property = node.get("property").textValue();
        String direction = node.get("direction").textValue();
        return new Order(Direction.fromOptionalString(direction).orElse(null), property);
    }

    @Override
    public Class<Sort> handledType() {
        return Sort.class;
    }
}
