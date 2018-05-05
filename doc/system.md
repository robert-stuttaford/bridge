## bridge - a ClojureBridge event management system

### Priorities

Own profile access and management
- ~~Create~~
- ~~Login~~

New Events
- ~~Create~~
- Register
- Close registration
- Send invites
- Manage attendee / coach state
- Make learning groups

Own chapter management
- Email templates

All chapters management

### Views and Verbs, by capability and role

#### Profiles (all users)

- ~~Create profile~~
- ~~Login~~
- ~~Reset password~~
- Update profile details
- Delete profile
- Views
  - Profile details

#### Events

##### Organiser

- ~~Add~~, ~~modify~~, remove events
- Close event registration
- Modify attendee state to confirmed, cancelled
- Modify coach state to accepted, declined
- Add note to attendee
- Send invitation message to participants (with expiry)
- Group attendees
- Views
  - Past vs Scheduled / active
  - ~~Event details~~
  - Attendees
    - List Filters:
      - Coach?
      - State
    - Detail + history of activity
    - Groupings
    - Previous event status
      - Registered before, but not invited? (typically invited ahead of new registrants)
      - Attended before? (typically invited after the above category, and new registrants)
  - Reports
    - T-shirts
    - Food preferences
    - Accessibility
    - Child care

##### Attendee

- Register for event (asked to update profile details at the same time)
- Modify attendee state to accepted or rejected (but only if invited)
- Modify coach state to willing, or declined
- Views
  - Past
  - Scheduled/active
    - Coach?
    - Status
    - Detail + history of activity
    - Group members, incl. which are coaches

#### Chapters

##### Admin

- Add, modify, remove chapters
- Invite organiser to chapter
- Unlink organiser
- Views
  - All chapters
  - Chapter details

##### Organiser

- Modify chapter details
- Invite organiser to chapter
- Unlink self from chapter
- Add, modify, remove email templates
- Views
  - Chapter memberships
  - Chapter details
