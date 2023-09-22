package nextstep.subway.unit;

import nextstep.subway.domain.Line;
import nextstep.subway.domain.Path;
import nextstep.subway.domain.Section;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static nextstep.subway.fixture.LineFixture.*;
import static nextstep.subway.fixture.StationFixture.*;
import static org.assertj.core.api.Assertions.assertThat;

public class PathTest {
    Line 이호선;
    Line 삼호선;
    Line 신분당선;

    Section 강남역_교대역_구간;
    Section 교대역_남부터미널역_구간;
    Section 남부터미널역_양재역_구간;
    Section 양재역_강남역_구간;


    /**
     * 노선 옆에 "()"는 구간의 거리를 의미합니다.
     * <p>
     * 교대역    --- *2호선*(3) ---   강남역
     * |                           |
     * *3호선*(4)                *신분당선*(5)
     * |                            |
     * 남부터미널역  --- *3호선*(2) ---   양재역
     */
    @BeforeEach
    void setup() {
        // 노선이 등록되어있다.
        이호선 = new Line(이호선_이름, 이호선_색);
        삼호선 = new Line(삼호선_이름, 삼호선_색);
        신분당선 = new Line(신분당선_이름, 신분당선_색);
        // 노선에 구간이 등록되어 있다.
        강남역_교대역_구간 = new Section(이호선, 강남역, 교대역, 3);
        교대역_남부터미널역_구간 = new Section(삼호선, 교대역, 남부터미널역, 4);
        남부터미널역_양재역_구간 = new Section(삼호선, 남부터미널역, 양재역, 2);
        양재역_강남역_구간 = new Section(신분당선, 양재역, 강남역, 5);
    }

    @Test
    @DisplayName("구간들을 넣고 출발역과 도착역을 입력하면 최단경로의 역목록 반환")
    void findPathStations() {
        // given 구간들을 입력
        List<Section> sections = Arrays.asList(강남역_교대역_구간, 교대역_남부터미널역_구간, 남부터미널역_양재역_구간, 양재역_강남역_구간);
        Path path = new Path(sections);

        // when 출발역과 도착역을 넣고 경로를 계산하면
        path.setSourceAndTarget(교대역, 양재역);

        // then 최단경로의 역 목록이 반환된다.
        assertThat(path.findPathStations()).containsExactly(교대역, 남부터미널역, 양재역);
    }
}
