package nextstep.subway.domain;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;
import static org.apache.commons.lang3.ObjectUtils.isEmpty;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import nextstep.subway.enums.LineColor;
import nextstep.subway.exception.DupSectionException;
import nextstep.subway.exception.LineNotFoundException;
import nextstep.subway.exception.SectionInvalidException;
import nextstep.subway.exception.StationNotFoundException;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;

@Entity
public class Line extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    private String name;

    @Enumerated(STRING)
    private LineColor lineColor;

    @Embedded
    private Distance distance;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "UP_STATION_ID", foreignKey = @ForeignKey(name = "fk_line_up_station"))
    private Station upStation;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "DOWN_STATION_ID", foreignKey = @ForeignKey(name = "fk_line_down_station"))
    private Station downStation;

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<LineStation> lineStationList = new LinkedHashSet<>();

    @OneToMany(mappedBy = "line", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private List<Section> sections = new LinkedList<>();

    public Line(Long id, String name, LineColor lineColor, Station upStation, Station downStation,
        Distance distance) {
        this.id = id;
        this.name = name;
        this.lineColor = lineColor;
        this.upStation = upStation;
        this.downStation = downStation;
        this.distance = distance;
    }

    protected Line() {
    }

    public void update(Line line) {
        if (StringUtils.isNotEmpty(line.getName())) {
            this.name = line.getName();
        }
        if (ObjectUtils.isNotEmpty(line.getLineColor())) {
            this.lineColor = line.getLineColor();
        }
        if (ObjectUtils.isNotEmpty(line.getDownStation())) {
            addStation(line.getDownStation());
            this.downStation = line.getDownStation();
        }
        if (ObjectUtils.isNotEmpty(line.getUpStation())) {
            addStation(line.getUpStation());
            this.upStation = line.getUpStation();
        }
    }

    public void addStation(Station station) {
        if (!lineStationList.contains(station)) {
            lineStationList.add(new LineStation(this, station));
        }
    }


    public void addSection(Section section) {
        validateDupSection(section);
        int first = 0;
        int last = sections.size() - 1;

        List<Section> sections = getSectionsByOrder();
        if (sections.get(first).getDownStation() == section.getUpStation()) {
            changeDownStation(sections.get(first), section);
            return;
        }
        if (sections.get(last).getUpStation() == section.getDownStation()) {
            changeUpStation(sections.get(last), section);
            return;
        }

        for (Section curSection : sections) {
            if (curSection.insert(section)) {
                return;
            }
        }
    }

    private void changeUpStation(Section originSection, Section appendSection) {
        this.distance.plus(appendSection.getDistance());
        this.upStation = appendSection.getUpStation();
        originSection.appendAfterSection(appendSection);
    }

    private void changeDownStation(Section originSection, Section appendSection) {
        this.distance.plus(appendSection.getDistance());
        this.downStation = appendSection.getDownStation();
        originSection.appendBeforeSection(appendSection);
    }

    private List<Section> getSectionsByOrder() {
        List<Section> sectionOrdered = new LinkedList<>();
        Section section = sections.stream()
            .filter(Section::isFirstSection)
            .findFirst()
            .orElseThrow(LineNotFoundException::new);
        while (ObjectUtils.isNotEmpty(section)) {
            sectionOrdered.add(section);
            section = section.getNextSection();
        }
        return sectionOrdered;
    }

    private void validateDupSection(Section section) {
        Optional<Section> sectionAlreadyPersistCheck = sections.stream().filter(
                o -> o.getDownStation().equals(section.getDownStation()) && o.getUpStation()
                    .equals(section.getUpStation()))
            .findFirst();
        if (sectionAlreadyPersistCheck.isPresent()) {
            throw new DupSectionException();
        }
    }

    public void replaceSectionByMerge(Section backSection, Section nextSection) {
        Section newSection = Section.merge(backSection, nextSection);
        deleteBackSectionInfo(backSection, newSection);
        deleteNextSectionInfo(nextSection, newSection);
        sections.add(newSection);
    }

    private void deleteNextSectionInfo(Section nextSection, Section newSection) {
        try {
            nextSection.getNextSection().setBackSection(newSection);
        } catch (NullPointerException e) {

        } finally {
            sections.remove(nextSection);
        }

    }

    private void deleteBackSectionInfo(Section backSection, Section newSection) {
        try {
            backSection.getBackSection().setNextSection(newSection);
        } catch (NullPointerException e) {

        } finally {
            sections.remove(backSection);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public LineColor getLineColor() {
        return lineColor;
    }

    public Station getUpStation() {
        return upStation;
    }

    public Station getDownStation() {
        return downStation;
    }


}
