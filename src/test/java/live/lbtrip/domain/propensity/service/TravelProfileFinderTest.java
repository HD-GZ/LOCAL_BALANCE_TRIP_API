package live.lbtrip.domain.propensity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.propensity.model.TravelProfile;
import live.lbtrip.domain.propensity.model.vo.LocalizedTravelProfile;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.global.i18n.MessageResolver;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.TravelProfileFixture;

@ExtendWith(MockitoExtension.class)
class TravelProfileFinderTest {

    @Mock
    private TravelProfileRepository travelProfileRepository;

    @Mock
    private MessageResolver messageResolver;

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

    @Nested
    class 로케일별_조회 {

        @Test
        void 한국어_로케일이면_한글_별칭과_설명으로_조회한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.KOREAN);
            when(travelProfileRepository.findByCode(TravelProfileFixture.CODE))
                .thenReturn(Optional.of(TravelProfileFixture.travelProfileWithEnglish()));

            LocalizedTravelProfile profile = travelProfileFinder.findLocalizedByPreference(PropensityFixture.preference());

            assertThat(profile.nickname()).isEqualTo(TravelProfileFixture.NICKNAME);
            assertThat(profile.description()).isEqualTo(TravelProfileFixture.DESCRIPTION);
        }

        @Test
        void 영어_로케일이면_영문_별칭과_설명으로_조회한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(travelProfileRepository.findByCode(TravelProfileFixture.CODE))
                .thenReturn(Optional.of(TravelProfileFixture.travelProfileWithEnglish()));

            LocalizedTravelProfile profile = travelProfileFinder.findLocalizedByPreference(PropensityFixture.preference());

            assertThat(profile.nickname()).isEqualTo(TravelProfileFixture.NICKNAME_EN);
            assertThat(profile.description()).isEqualTo(TravelProfileFixture.DESCRIPTION_EN);
        }

        @Test
        void 영어_로케일이어도_영문_번역이_없으면_한글로_폴백한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(travelProfileRepository.findByCode(TravelProfileFixture.CODE))
                .thenReturn(Optional.of(TravelProfileFixture.travelProfile()));

            LocalizedTravelProfile profile = travelProfileFinder.findLocalizedByPreference(PropensityFixture.preference());

            assertThat(profile.nickname()).isEqualTo(TravelProfileFixture.NICKNAME);
            assertThat(profile.description()).isEqualTo(TravelProfileFixture.DESCRIPTION);
        }
    }

    @Nested
    class 대표_유형_조회 {

        @Test
        void 대표_유형을_현재_로케일로_조회한다() {
            when(messageResolver.currentLocale()).thenReturn(Locale.ENGLISH);
            when(travelProfileRepository.findByFeaturedOrderIsNotNullOrderByFeaturedOrderAsc())
                .thenReturn(List.of(TravelProfileFixture.travelProfileWithEnglish()));

            List<LocalizedTravelProfile> profiles = travelProfileFinder.findFeatured();

            assertThat(profiles).hasSize(1);
            assertThat(profiles.get(0).nickname()).isEqualTo(TravelProfileFixture.NICKNAME_EN);
        }
    }
}
