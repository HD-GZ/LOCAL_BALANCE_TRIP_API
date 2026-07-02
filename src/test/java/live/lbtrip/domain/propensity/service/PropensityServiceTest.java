package live.lbtrip.domain.propensity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import live.lbtrip.domain.propensity.dto.response.PropensityResponse;
import live.lbtrip.domain.propensity.model.Propensity;
import live.lbtrip.domain.propensity.model.PropensityBucket;
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.propensity.repository.TravelProfileRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.PropensityRequestFixture;
import live.lbtrip.support.fixture.PropensityResponseFixture;
import live.lbtrip.support.fixture.TravelProfileFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class PropensityServiceTest {

    @Mock
    private PropensityRepository propensityRepository;

    @Mock
    private TravelProfileRepository travelProfileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PropensityService propensityService;

    @Nested
    class 등록 {

        @Test
        void 기존_결과가_없으면_새_결과를_저장한다() {
            User user = UserFixture.user();
            Propensity propensity = PropensityFixture.propensity(
                user,
                PropensityFixture.preference(),
                PropensityFixture.valueConsumption()
            );
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.empty());
            when(userRepository.getReferenceById(AuthResponseFixture.USER_ID)).thenReturn(user);
            when(propensityRepository.save(any(Propensity.class))).thenReturn(propensity);
            when(travelProfileRepository.findByBuckets(
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.HIGH)
            )).thenReturn(Optional.of(TravelProfileFixture.travelProfile()));

            PropensityResponse response = propensityService.setPropensity(
                AuthResponseFixture.USER_ID,
                PropensityRequestFixture.propensityRequest()
            );

            verify(propensityRepository).save(any(Propensity.class));
            assertThat(response.propensityResult().type()).isEqualTo(PropensityResponseFixture.TYPE);
            assertThat(response.propensityResult().description()).isEqualTo(PropensityResponseFixture.DESCRIPTION);
            assertThat(response.preference().locality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(response.preference().sociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
            assertThat(response.valueConsumption().accommodation()).isEqualTo(PropensityRequestFixture.ACCOMMODATION);
            assertThat(response.valueConsumption().cafeExhibition()).isEqualTo(PropensityRequestFixture.CAFE_EXHIBITION);
        }

        @Test
        void 기존_결과가_있으면_점수를_갱신한다() {
            Propensity propensity = PropensityFixture.propensity();
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(propensity));
            when(travelProfileRepository.findByBuckets(
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.NEUTRAL),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.NEUTRAL),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW)
            )).thenReturn(Optional.of(TravelProfileFixture.travelProfile()));

            PropensityResponse response = propensityService.setPropensity(
                AuthResponseFixture.USER_ID,
                PropensityRequestFixture.updatedPropensityRequest()
            );

            verify(propensityRepository, never()).save(any(Propensity.class));
            assertThat(response.propensityResult().type()).isEqualTo(PropensityResponseFixture.TYPE);
            assertThat(response.propensityResult().description()).isEqualTo(PropensityResponseFixture.DESCRIPTION);
            assertThat(response.preference().locality()).isEqualTo(PropensityRequestFixture.UPDATED_LOCALITY);
            assertThat(response.preference().frugality()).isEqualTo(PropensityRequestFixture.UPDATED_FRUGALITY);
            assertThat(response.preference().experientiality()).isEqualTo(PropensityRequestFixture.UPDATED_EXPERIENTIALITY);
            assertThat(response.preference().vitality()).isEqualTo(PropensityRequestFixture.UPDATED_VITALITY);
            assertThat(response.preference().sociality()).isEqualTo(PropensityRequestFixture.UPDATED_SOCIALITY);
            assertThat(response.valueConsumption().accommodation()).isEqualTo(PropensityRequestFixture.UPDATED_ACCOMMODATION);
            assertThat(response.valueConsumption().food()).isEqualTo(PropensityRequestFixture.UPDATED_FOOD);
            assertThat(response.valueConsumption().experience()).isEqualTo(PropensityRequestFixture.UPDATED_EXPERIENCE);
            assertThat(response.valueConsumption().transportation()).isEqualTo(PropensityRequestFixture.UPDATED_TRANSPORTATION);
            assertThat(response.valueConsumption().cafeExhibition()).isEqualTo(PropensityRequestFixture.UPDATED_CAFE_EXHIBITION);
        }
    }

    @Nested
    class 조회 {

        @Test
        void 취향_진단_결과를_조회한다() {
            Propensity propensity = PropensityFixture.propensity();
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(propensity));
            when(travelProfileRepository.findByBuckets(
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.HIGH),
                eq(PropensityBucket.LOW),
                eq(PropensityBucket.HIGH)
            )).thenReturn(Optional.of(TravelProfileFixture.travelProfile()));

            PropensityResponse response = propensityService.getPropensity(AuthResponseFixture.USER_ID);

            assertThat(response.propensityResult().type()).isEqualTo(PropensityResponseFixture.TYPE);
            assertThat(response.propensityResult().description()).isEqualTo(PropensityResponseFixture.DESCRIPTION);
            assertThat(response.preference().locality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(response.preference().frugality()).isEqualTo(PropensityRequestFixture.FRUGALITY);
            assertThat(response.preference().experientiality()).isEqualTo(PropensityRequestFixture.EXPERIENTIALITY);
            assertThat(response.preference().vitality()).isEqualTo(PropensityRequestFixture.VITALITY);
            assertThat(response.preference().sociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
            assertThat(response.valueConsumption().accommodation()).isEqualTo(PropensityRequestFixture.ACCOMMODATION);
            assertThat(response.valueConsumption().food()).isEqualTo(PropensityRequestFixture.FOOD);
            assertThat(response.valueConsumption().experience()).isEqualTo(PropensityRequestFixture.EXPERIENCE);
            assertThat(response.valueConsumption().transportation()).isEqualTo(PropensityRequestFixture.TRANSPORTATION);
            assertThat(response.valueConsumption().cafeExhibition()).isEqualTo(PropensityRequestFixture.CAFE_EXHIBITION);
        }

        @Test
        void 취향_진단_결과가_없으면_예외를_던진다() {
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> propensityService.getPropensity(AuthResponseFixture.USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROPENSITY_NOT_FOUND);
        }

        @Test
        void 여행_프로필이_없으면_예외를_던진다() {
            Propensity propensity = PropensityFixture.propensity();
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(propensity));
            when(travelProfileRepository.findByBuckets(
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class),
                any(PropensityBucket.class)
            )).thenReturn(Optional.empty());

            assertThatThrownBy(() -> propensityService.getPropensity(AuthResponseFixture.USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.TRAVEL_PROFILE_NOT_FOUND);
        }
    }
}
