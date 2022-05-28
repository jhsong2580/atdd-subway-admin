package nextstep.subway.domain;

import static javax.persistence.EnumType.STRING;
import static javax.persistence.FetchType.LAZY;
import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Entity;
import javax.persistence.Enumerated;
import javax.persistence.ForeignKey;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import nextstep.subway.enums.LineColor;
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

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "UP_STATION_ID", foreignKey = @ForeignKey(name = "fk_line_up_station"))
    private Station upStation;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "DOWN_STATION_ID", foreignKey = @ForeignKey(name = "fk_line_down_station"))
    private Station downStation;

    public void update(Line line) {
        if(StringUtils.isNotEmpty(line.getName())){
            this.name = line.getName();
        }
        if(ObjectUtils.isNotEmpty(line.getLineColor())){
            this.lineColor = line.getLineColor();
        }
        if(ObjectUtils.isNotEmpty(line.getDownStation())){
            this.downStation = line.getDownStation();
        }
        if(ObjectUtils.isNotEmpty(line.getUpStation())){
            this.upStation = line.getUpStation();
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

    public Line(Long id, String name, LineColor lineColor, Station upStation, Station downStation) {
        this.id = id;
        this.name = name;
        this.lineColor = lineColor;
        this.upStation = upStation;
        this.downStation = downStation;
    }

    protected Line() {
    }
}
