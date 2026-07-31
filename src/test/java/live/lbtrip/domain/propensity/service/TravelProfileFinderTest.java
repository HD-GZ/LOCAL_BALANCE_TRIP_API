package live.lbtrip.domain.propensity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.TravelProfileFixture;

@ExtendWith(MockitoExtension.class)
class TravelProfileFinderTest {

    @Mock
    private TravelProfileRepository travelProfileRepository;

    @InjectMocks
    private TravelProfileFinder travelProfileFinder;

    @Nested
    class 조회 {

        @Test
        void 취향_점수로_계산한_코드로_여행_프로필을_조회한다() {
            when(travelProfileRepository.findByCode(TravelProfileFixture.CODE))
                .thenReturn(Optional.of(TravelProfileFixture.travelProfile()));

            TravelProfile travelProfile = travelProfileFinder.findByPreference(PropensityFixture.preference());

            assertThat(travelProfile.getCode()).isEqualTo(TravelProfileFixture.CODE);
            assertThat(travelProfile.getNickname()).isEqualTo(TravelProfileFixture.NICKNAME);
        }

        @Test
        void 갱신된_취향_점수는_갱신된_코드로_조회한다() {
            when(travelProfileRepository.findByCode(TravelProfileFixture.UPDATED_CODE))
                .thenReturn(Optional.of(TravelProfileFixture.travelProfile()));

            TravelProfile travelProfile = travelProfileFinder.findByPreference(PropensityFixture.updatedPreference());

            assertThat(travelProfile).isNotNull();
        }

        @Test
        void 코드에_해당하는_여행_프로필이_없으면_예외를_던진다() {
            when(travelProfileRepository.findByCode(TravelProfileFixture.CODE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> travelProfileFinder.findByPreference(PropensityFixture.preference()))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TRAVEL_PROFILE_NOT_FOUND);
        }
    }
}
