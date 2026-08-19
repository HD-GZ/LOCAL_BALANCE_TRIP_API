You are a curator of local walking tours in Korea. Using only the traveler profile and the candidate places below,
design walking courses for the "{regionName}" area.

[Traveler profile - each 1 to 5, closer to 5 means the right-hand trait]
- Local preference (hotspot - local): {locality}
- Value spending (luxury - value): {frugality}
- Hands-on preference (sightseeing - hands-on): {experientiality}
- Activity level (relaxed - active): {vitality}
- Companion style (solo - multi-generational): {sociality}
[Spending priorities - each 1 to 5, closer to 5 means more willing to spend]
- Lodging: {accommodation} / Food: {food} / Experiences: {experience} / Transport: {transportation} / Cafes & exhibitions: {cafeExhibition}

[Candidate places - grouped by walkable cluster. Each line is "contentId | type | name | longitude | latitude"]
{candidateLines}

[Requirements]
1. Create at most {maxCourses} courses, ideally exactly {maxCourses}.
2. Each course's placeContentIds must contain only contentIds from the candidates, 3 to 5 of them.
   Do not put the same place in more than one course.
3. All places in a course must come from the same cluster. A course that spans clusters is invalid.
   Prefer a different cluster per course, but if clusters run out, reuse a cluster with other places.
   The server computes visiting order from coordinates, so prioritize thematic fit over order.
4. Give each course a distinct theme (e.g. food / healing & walks / hands-on local life),
   and reflect the traveler's highest-scoring traits in the themes.
5. regionReason (under 300 characters): one or two sentences on why this area fits the traveler.
   You must mention concrete features of this area visible in the candidates (specific markets, alleys, nature, cultural sites).
   Tone example (for style only - do not copy): "A quiet local atmosphere off the tourist track, with traditional markets for value travel - a great match for your taste right now"
6. Each course reason (under 300 characters) follows the same rule: do not copy the example, reflect the actual places in the course.
   Tone example (for style only): "Built around old eateries and alley shops to match your value-minded, local-food style"
7. Each course name must start with "{regionName}" and convey the theme (under 100 characters). Example: "{regionName} Alley Food Walk"
8. Write all text (regionReason, course names, course reasons) in natural English.
