package nextstep.subway.domain;

import nextstep.subway.common.exception.line.ContainsAllStationException;
import nextstep.subway.common.exception.line.OnlyOneSectionException;
import nextstep.subway.common.exception.station.NotExistStationException;

import javax.persistence.*;
import java.util.*;

@Embeddable
public class Sections {

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "start_station_id")
    private Station startStation;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "end_station_id")
    private Station endStation;

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST,
            CascadeType.MERGE}, orphanRemoval = true)
    private List<Section> sections;


    public Sections() {
        sections = new ArrayList<>();
    }

    public List<Section> getSections() {
        return sections;
    }

    public List<Station> getStations() {
        if (sections.isEmpty()) {
            return Collections.emptyList();
        }

        List<Station> stations = new ArrayList<>();
        Station nowStation = startStation;
        stations.add(nowStation);
        while (!nowStation.equals(endStation)) {
            for (Section section : sections) {
                if (section.getUpStation().equals(nowStation)) {
                    nowStation = section.getDownStation();
                    stations.add(nowStation);
                }
            }
        }
        return stations;
    }

    public Station getStartStation() {
        return startStation;
    }

    public Station getEndStation() {
        return endStation;
    }

    public void addSection(Section section) {
        if (hasAllStations(section)) {
            throw new ContainsAllStationException();
        }
        if (this.sections.isEmpty()) {
            addFirstSection(section);
            return;
        }
        if (this.startStation.equals(section.getDownStation())) {
            addStartSection(section);
            return;
        }
        if (this.endStation.equals(section.getUpStation())) {
            addEndSection(section);
            return;
        }
        addMiddleSection(section);
    }


    public void removeSection(Station station) {
        if (sections.size() <= 1) {
            throw new OnlyOneSectionException();
        }
        if (!this.getStations().contains(station)) {
            throw new NotExistStationException();
        }
        if (startStation.equals(station)) {
            removeStartStation(station);
            return;
        }
        if (endStation.equals(station)) {
            removeEndStation(station);
            return;
        }
        removeMiddleStation(station);
    }

    private boolean hasAllStations(Section section) {
        return new HashSet<>(this.getStations()).containsAll(List.of(section.getUpStation(), section.getDownStation()));
    }

    private void addFirstSection(Section section) {
        this.startStation = section.getUpStation();
        this.endStation = section.getDownStation();
        sections.add(section);
    }

    private void addStartSection(Section section) {
        this.startStation = section.getUpStation();
        sections.add(section);
    }

    private void addEndSection(Section section) {
        this.endStation = section.getDownStation();
        sections.add(section);
    }

    private void addMiddleSection(Section section) {
        Section oldSection = getSectionByDownStation(section.getDownStation())
                .or(() -> getSectionByUpStation(section.getUpStation()))
                .orElseThrow(NotExistStationException::new);
        oldSection.splitSection(section);
        sections.add(section);
    }

    private void removeStartStation(Station station) {
        this.getSectionByUpStation(station).ifPresent(section -> {
            startStation = section.getDownStation();
            sections.remove(section);
        });
    }

    private void removeEndStation(Station station) {
        this.getSectionByDownStation(station).ifPresent(section -> {
            endStation = section.getUpStation();
            sections.remove(section);
        });
    }

    private void removeMiddleStation(Station station) {
        Section upSection = getSectionByDownStation(station).get();
        Section downSection = getSectionByUpStation(station).get();
        upSection.unionDownSection(downSection);
        sections.remove(downSection);
    }

    private Optional<Section> getSectionByDownStation(Station station) {
        return sections.stream()
                .filter(section -> section.getDownStation().equals(station))
                .findFirst();
    }

    private Optional<Section> getSectionByUpStation(Station station) {
        return sections.stream()
                .filter(section -> section.getUpStation().equals(station))
                .findFirst();
    }
}
