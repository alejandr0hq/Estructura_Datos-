package airctrl.model;

import java.util.List;

public record Route(List<String> locations, int distanceMeters) {
    public Route {
        locations = List.copyOf(locations);
    }
}
