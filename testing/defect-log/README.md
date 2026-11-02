# Defect Log

Stage 9 testing found no reproducible application defect.

| ID | Finding | Resolution / status |
|---|---|---|
| QA-01 | Initial Selenium request form selector did not match the rendered submit button. | Corrected the test selector. |
| QA-02 | Browser test expected an alert to change after a task status update, although the displayed alert may retain the previous notice while the assignment refreshes. | Test now waits for the assignment’s rendered status. |
| QA-03 | Chrome disconnected in one combined Maven verify run after the focused Selenium scenario had passed. | Run the Selenium scenario separately as documented; backend suite remains green with Selenium opt-in skipped. Track this as test-environment instability if it recurs. |

No user-facing workflow issue was confirmed from these test-harness findings. The final focused Selenium run succeeded end to end.
