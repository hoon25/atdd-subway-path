package nextstep.subway.domain;

import nextstep.subway.common.exception.pathFinder.DisconnectedPathException;
import nextstep.subway.common.exception.pathFinder.SameStationException;
import org.jgrapht.GraphPath;
import org.jgrapht.alg.shortestpath.DijkstraShortestPath;
import org.jgrapht.graph.WeightedMultigraph;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PathFinder {
    private final List<Line> lines;

    public PathFinder(List<Line> lines) {
        this.lines = lines;
    }

    public Path findPath(Station source, Station target) {
        if (source.equals(target)) {
            throw new SameStationException();
        }
        WeightedMultigraph<Station, SectionEdge> graph = new WeightedMultigraph<>(
                SectionEdge.class
        );
        lines.stream()
                .flatMap(it -> it.getStations().stream())
                .distinct()
                .collect(Collectors.toList())
                .forEach(graph::addVertex);

        lines.stream()
                .flatMap(it -> it.getSections().getSections().stream())
                .forEach(it -> {
                    SectionEdge sectionEdge = SectionEdge.of(it);
                    graph.addEdge(it.getUpStation(), it.getDownStation(), sectionEdge);
                    graph.setEdgeWeight(sectionEdge, it.getDistance());
                });
        DijkstraShortestPath<Station, SectionEdge> dijkstraShortestPath = new DijkstraShortestPath(graph);
        GraphPath<Station, SectionEdge> result = dijkstraShortestPath.getPath(source, target);

        if (result == null) {
            throw new DisconnectedPathException();
        }
        List<Section> pathSections = new ArrayList<>(result.getEdgeList().stream()
                .map(it -> it.getSection())
                .collect(Collectors.toList()));
        return new Path(new Sections(pathSections, source, target));
    }
}
