# Firestore security review — local candidate, not ready to publish

The deployed rules supplied for this review are time-limited test-mode rules. The local `firestore.rules` is a restrictive candidate; it is **not** a complete, regression-tested replacement for this application. Do not deploy it until the blockers below are addressed. No deployment was performed.

## Inventory and intended access

| Collection or path | Existing access and candidate policy |
| --- | --- |
| `users/{uid}` | Registration creates the user's own student profile. Users read their profile and may change name/institution only. Staff can read college-tagged profiles in their own college. Authentication claims, not the profile, determine authority. |
| `users/{uid}/savedPlacements/{opportunityId}` | Candidate private bookmark schema. The current saved-placement control is local state and does not use this path. |
| `companies` | Read approved companies of the caller's claimed college; Admin SDK callable performs writes. |
| `placementDrives`, `opportunities` | College-scoped reads; client writes denied. `publishPlacementDrive` writes via Admin SDK. |
| `verificationRequests` | College staff read; staff may create a pending request; a college admin may transition its status. The current approval buttons only update local UI state and do not publish/verify a drive. |
| `notifications` | Candidate schema requires `collegeId` and recipient `userId`; recipient may only change `isRead`. Existing app only reads. |
| `applications` | Candidate schema requires `collegeId` and `userId`, and a verified same-college opportunity. Owners and same-college staff can read; no client updates or deletion. Current Apply button does not call the repository. |
| `misinformationReports` | Candidate schema requires `collegeId` and reporter UID. Owners and same-college staff read, staff may update status. Current report form does not persist its submission. |
| `interviewResearch` | Legacy read only, tied to a verified same-college opportunity. No client writes. |
| `researchJobs`, `preparationKits` and `interviewQuestions` / `interviewExperiences` children | College-scoped read; backend Admin SDK exclusively writes. A missing deterministic kit may be probed by a user with a college claim. |
| `studentPreparationProgress/{uid}/questions/{question}` | Owner-only bookmark/completion data. |
| `researchProviderState`, `researchRateLimits` | Backend only; no matching client allow rule. |
| All other paths | No client access. |

Repository queries for opportunities, applications, notifications, reports and verification requests now include required college and/or user constraints. Firestore rules do not filter unsafe queries. Proposed composite indexes are in `firestore.indexes.json`.

## Release blockers

1. Existing college/role claims must be provisioned only by a trusted administrator after membership review; new student self-registration deliberately does not grant a college claim. No student with no claim can read college data.
2. Inventory all production documents and other clients, which cannot be inspected from this checkout. Existing legacy records may lack `collegeId`, `userId`, or a reliable `reportedBy` UID. Data under the previous test-mode rules could have been written by anyone. Audit and quarantine untrusted records, approvals, profiles and verification flags; use an authorized, reviewed server-side migration. Never infer membership or trust from a user-entered institution or old verified flag.
3. Implement and test the actual saved-placement, application, report and verification UI flows with the candidate schemas. The existing detail screen changes save/apply state locally; report submission and admin verification buttons similarly do not persist. Admin user management is a placeholder. Admin analytics contains hardcoded values.
4. `publishPlacementDrive` currently sets `approved`, `verified`, and `isVerified` directly for an officer, bypassing the separate admin review shown in the UI. Firestore client rules cannot change Admin SDK behavior. Design and test a trusted pending/review/publish transaction before claiming approval workflow security. Review all Admin SDK handlers and IAM: these bypass Firestore rules.
5. Determine whether administrators are scoped to one college (the candidate policy) or need explicitly authorized cross-college administration. Never grant cross-college access by default.
6. Test all real app operations and queries for all three roles, local registration without claims, profile recovery, data migration, callable App Check, offline and error states. Build and verify indexes, and perform independent security review before requesting permission to publish. Repository methods currently catch several errors and return empty lists, which can hide denied access or missing claims.
7. Review profile data and rules against production schemas: the candidate allows user updates only to name/institution, treats institution as untrusted display text, and disallows role/college changes. Existing clients may need coordinated changes.
