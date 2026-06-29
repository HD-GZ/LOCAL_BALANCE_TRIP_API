package live.lbtrip.domain.propensity.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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
import live.lbtrip.domain.propensity.repository.PropensityRepository;
import live.lbtrip.domain.user.model.User;
import live.lbtrip.domain.user.repository.UserRepository;
import live.lbtrip.global.error.BusinessException;
import live.lbtrip.global.error.ErrorCode;
import live.lbtrip.support.fixture.AuthResponseFixture;
import live.lbtrip.support.fixture.PropensityFixture;
import live.lbtrip.support.fixture.PropensityRequestFixture;
import live.lbtrip.support.fixture.PropensityResponseFixture;
import live.lbtrip.support.fixture.UserFixture;

@ExtendWith(MockitoExtension.class)
class PropensityServiceTest {

    @Mock
    private PropensityRepository propensityRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PropensityClassifier classifier;

    @InjectMocks
    private PropensityService propensityService;

    @Nested
    class 등록 {

        @Test
        void 기존_결과가_없으면_새_결과를_저장한다() {
            User user = UserFixture.user();
            Propensity propensity = PropensityFixture.propensity(user, PropensityFixture.scores());
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.empty());
            when(userRepository.getReferenceById(AuthResponseFixture.USER_ID)).thenReturn(user);
            when(propensityRepository.save(any(Propensity.class))).thenReturn(propensity);
            when(classifier.classify(PropensityFixture.scores())).thenReturn(PropensityResponseFixture.result());

            PropensityResponse response = propensityService.setPropensity(
                AuthResponseFixture.USER_ID,
                PropensityRequestFixture.propensityRequest()
            );

            verify(propensityRepository).save(any(Propensity.class));
            assertThat(response.result()).isEqualTo(PropensityResponseFixture.result());
            assertThat(response.locality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(response.sociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
        }

        @Test
        void 기존_결과가_있으면_점수를_갱신한다() {
            Propensity propensity = PropensityFixture.propensity();
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(propensity));
            when(classifier.classify(PropensityFixture.updatedScores())).thenReturn(PropensityResponseFixture.result());

            PropensityResponse response = propensityService.setPropensity(
                AuthResponseFixture.USER_ID,
                PropensityRequestFixture.updatedPropensityRequest()
            );

            verify(propensityRepository, never()).save(any(Propensity.class));
            assertThat(response.result()).isEqualTo(PropensityResponseFixture.result());
            assertThat(response.locality()).isEqualTo(PropensityRequestFixture.UPDATED_LOCALITY);
            assertThat(response.frugality()).isEqualTo(PropensityRequestFixture.UPDATED_FRUGALITY);
            assertThat(response.flexibility()).isEqualTo(PropensityRequestFixture.UPDATED_FLEXIBILITY);
            assertThat(response.experientiality()).isEqualTo(PropensityRequestFixture.UPDATED_EXPERIENTIALITY);
            assertThat(response.vitality()).isEqualTo(PropensityRequestFixture.UPDATED_VITALITY);
            assertThat(response.sociality()).isEqualTo(PropensityRequestFixture.UPDATED_SOCIALITY);
        }
    }

    @Nested
    class 조회 {

        @Test
        void 취향_진단_결과를_조회한다() {
            Propensity propensity = PropensityFixture.propensity();
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.of(propensity));
            when(classifier.classify(PropensityFixture.scores())).thenReturn(PropensityResponseFixture.result());

            PropensityResponse response = propensityService.getPropensity(AuthResponseFixture.USER_ID);

            assertThat(response.result()).isEqualTo(PropensityResponseFixture.result());
            assertThat(response.locality()).isEqualTo(PropensityRequestFixture.LOCALITY);
            assertThat(response.frugality()).isEqualTo(PropensityRequestFixture.FRUGALITY);
            assertThat(response.flexibility()).isEqualTo(PropensityRequestFixture.FLEXIBILITY);
            assertThat(response.experientiality()).isEqualTo(PropensityRequestFixture.EXPERIENTIALITY);
            assertThat(response.vitality()).isEqualTo(PropensityRequestFixture.VITALITY);
            assertThat(response.sociality()).isEqualTo(PropensityRequestFixture.SOCIALITY);
        }

        @Test
        void 취향_진단_결과가_없으면_예외를_던진다() {
            when(propensityRepository.findByUserId(AuthResponseFixture.USER_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> propensityService.getPropensity(AuthResponseFixture.USER_ID))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.PROPENSITY_NOT_FOUND);
        }
    }
}
