package airctrl.graph;

import airctrl.model.Connection;
import airctrl.model.Route;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

public final class AirportGraph {
    private final Map<String, List<Edge>> adjacency = new LinkedHashMap<>();
    private int validEdgeCount;

    public AirportGraph(Iterable<Connection> connections) {
        for (Connection connection : connections) {
            addConnectionRecord(connection.sourceLocation(), connection.targetLocation(),
                    connection.distanceMeters());
        }
    }

    public void addConnection(String source, String target, int distanceMeters) {
        String normalizedSource = normalize(source);
        String normalizedTarget = normalize(target);
        if (normalizedSource.isEmpty() || normalizedTarget.isEmpty()) {
            throw new IllegalArgumentException("Las ubicaciones no pueden estar vacias");
        }
        if (distanceMeters <= 0) {
            throw new IllegalArgumentException("La distancia debe ser mayor que cero");
        }
        addConnectionRecord(normalizedSource, normalizedTarget, distanceMeters);
    }

    public Optional<Route> shortestRoute(String source, String target) {
        String start = normalize(source);
        String end = normalize(target);
        requireLocation(start);
        requireLocation(end);

        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previous = new HashMap<>();
        PriorityQueue<Visit> pending = new PriorityQueue<>();
        distances.put(start, 0);
        pending.add(new Visit(start, 0));

        while (!pending.isEmpty()) {
            Visit current = pending.poll();
            if (current.distance() != distances.getOrDefault(current.location(), Integer.MAX_VALUE)) {
                continue;
            }
            if (current.location().equals(end)) {
                return Optional.of(buildRoute(start, end, current.distance(), previous));
            }
            for (Edge edge : adjacency.get(current.location())) {
                if (edge.distanceMeters() == null) {
                    continue;
                }
                int candidate = current.distance() + edge.distanceMeters();
                if (candidate < distances.getOrDefault(edge.target(), Integer.MAX_VALUE)) {
                    distances.put(edge.target(), candidate);
                    previous.put(edge.target(), current.location());
                    pending.add(new Visit(edge.target(), candidate));
                }
            }
        }
        return Optional.empty();
    }

    public List<String> breadthFirst(String source) {
        String start = normalize(source);
        requireLocation(start);
        List<String> order = new ArrayList<>();
        Set<String> visited = new LinkedHashSet<>();
        Queue<String> pending = new java.util.ArrayDeque<>();
        visited.add(start);
        pending.add(start);

        while (!pending.isEmpty()) {
            String current = pending.remove();
            order.add(current);
            for (Edge edge : adjacency.get(current)) {
                if (visited.add(edge.target())) {
                    pending.add(edge.target());
                }
            }
        }
        return order;
    }

    public int vertexCount() {
        return adjacency.size();
    }

    public int edgeCount() {
        return validEdgeCount;
    }

    public boolean hasLocation(String location) {
        return adjacency.containsKey(normalize(location));
    }

    private Route buildRoute(String start, String end, int distance,
                             Map<String, String> previous) {
        List<String> locations = new ArrayList<>();
        String current = end;
        while (current != null) {
            locations.add(current);
            if (current.equals(start)) {
                break;
            }
            current = previous.get(current);
        }
        Collections.reverse(locations);
        return new Route(locations, distance);
    }

    private void addConnectionRecord(String source, String target, Integer distanceMeters) {
        String normalizedSource = normalize(source);
        String normalizedTarget = normalize(target);
        if (normalizedSource.isEmpty() || normalizedTarget.isEmpty()) {
            return;
        }
        Integer validDistance = distanceMeters != null && distanceMeters > 0
                ? distanceMeters : null;
        adjacency.computeIfAbsent(normalizedSource, key -> new ArrayList<>())
                .add(new Edge(normalizedTarget, validDistance));
        adjacency.computeIfAbsent(normalizedTarget, key -> new ArrayList<>())
                .add(new Edge(normalizedSource, validDistance));
        if (validDistance != null) {
            validEdgeCount++;
        }
    }

    private void requireLocation(String location) {
        if (!adjacency.containsKey(location)) {
            throw new IllegalArgumentException("Ubicacion " + location + " no encontrada");
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toUpperCase();
    }

    private record Edge(String target, Integer distanceMeters) {
    }

    private record Visit(String location, int distance) implements Comparable<Visit> {
        @Override
        public int compareTo(Visit other) {
            return Integer.compare(distance, other.distance);
        }
    }
}
