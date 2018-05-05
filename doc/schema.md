## Schema

### user

- ~~state: active, suspended~~
- ~~preferred name~~
- ~~full name~~
- ~~email address~~
- ~~password~~
- ~~admin? (manage chapters)~~
- ~~minor or adult?~~
- ~~spoken languages~~
- ~~gender identity~~
- ~~agree to code of conduct?~~
- ~~food preferences~~
- ~~t-shirt size~~
- ~~attendees~~
  - ~~past programming experience~~
  - ~~experience with Clojure?~~
  - ~~been to ClojureBridge before?~~
- ~~coaches~~
  - ~~phone number~~
  - ~~coaching languages~~
  - ~~clojure experience~~
  - ~~past coaching experience~~
  - ~~background / other languages~~
  - ~~“who would you prefer to coach for?”~~
  - ~~floating coach OK?~~

### chapter

- ~~state: active, suspended~~
- ~~name~~
- ~~location~~
- email templates
- ~~events~~
- organiser invites
- ~~organisers~~ (manage own chapter, its organisers, and its events)

### email template

- label
- subject
- content

### chapter organiser invite

- state: pending, accepted
- user
- expiry date

### event

- ~~state: draft, registering, inviting, in progress, cancelled, complete~~
- ~~slug~~
- ~~registration close date~~
- ~~begin date~~
- ~~end date~~
- ~~event details - markdown~~
- ~~internal notes - markdown~~
- ~~organisers~~
- participants
- coaches
- groupings

### event participant

- state: registered, invited, confirmed, cancelled, attended
- Coach state: willing, accepted, declined
- willing to coach?
- needs child-care?
- plan to travel from afar?
- health issues / extra assistance required
- organiser notes - markdown
- invitation expiry date
- sent messages
- how did you hear about this event?

### event participant message

- send date
- content
- recipient

### event coach

- event participant
- floating?

### event grouping

- spoken language
- event participants
- event coaches
