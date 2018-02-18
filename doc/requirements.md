## Actors

Organizers

- Open and close the sign up forms
- Can see the list of registered attendees and coaches
- Can change the “state” of specific attendees or coaches
- Create/edit groups
- Can retrieve summarized information of all confirmed attendees/coaches, e.g. food preferences or t-shirt sizes

Attendees

- Register by filling out a registration form
- Can accept or decline after being invited
- Can see the state of their application
- Can see their group, especially who their coaches are

Coaches

- Register by filling out the coach registration form
- Can withdraw their application
- Can see if they have been selected
- Can see their group details

## Process

For each workshop, the use of the app goes through a cycle that starts with the opening of registration, and ends on workshop day.

1. Registration
   For a period ranging from a few weeks to a month or two, potential attendees and coaches can register by filling out the respective sign-up forms
2. Inviting and Confirming
   A subset of the registered attendees is invited to attend, and asked to confirm their presence. If there are more coaches registered than needed than a subset of the registered coaches gets invited.
3. Group formation
   Coaches and attendees are split into groups, which typically consist of 2 coaches and 4 attendees.

### Attendee state

An attendee’s application can be in one of four states:

Waiting

After signing up the attendee is in the “waiting” state (on the waiting list). They have no guarantee that they will be able to attend yet.

Invited

After registration closes a number of applicants are “invited”, they are sent an email asking them to confirm their presence within a certain timeframe. Inviting people can be both manual or automatic.

People who get preference, e.g. because they were rejected in the past, get invited first. After that people are invited automatically. We tell the system how many spots there are to be filled, and it will randomly pick that many people from the “waiting” list, and invite them.

The last one or two days before the workshop if more people need to be invited then this is done manually again. We pick people we have a personal connection with, to maximize the odds of them showing up last minute.

Accepted

Once an invited person confirms their attendance within the given time limit they are set to “accepted”. Now they can be assigned to groups, their data will show up in the summaries (see below), etc.

They may still cancel between being accepted and workshop day, e.g. due to illness.

Cancelled

A person in the “cancelled” state is no longer in the running to participate. After registration closes the application data is reviewed, and people who are not eligible to participate are set to “cancelled”.

Invited people are set to “cancelled” when they either decline the invitation, their invitation “times out” (they don’t respond within the time limit), or when they cancel after being accepted, e.g. due to illness.

### Coach state

The process for coaches has so far been less involved, because we tend to have roughly the amount of coaches sign up that are needed. We still need some way to track which coaches are “in” vs “out”. A coach’s attendance may be declined, for instance

- They are a first time coach and are unable to attend the coaches training
- The organization does not feel comfortable having a certain individual attend
- There are more coaches than needed. In this cases women are typically picked first.

### Inviting attendees

Inviting attendees can either be done manually, semi-automatic, or fully automatic.

#### Manually

This means selecting a specific attendee, and setting them to invited. This is occasionally done when an attendee applied in the past and failed to get in.

#### Semi-automatic

This is what the attendomat (google sheets version) currently does, it asks you “how many people do you want to invite?”, and it will pick that many people from the waiting list at random and invites them. Whenever people have cancelled or “time out” someone from the organization will repeat this process, inviting a few more.

#### Fully Automatic

If the app automatically sends out emails to invited attendees, with buttons/links allowing them to confirm/decline by themselves, then we could go one step further and have the app automatically invite more people as soon as people cancel or they “time out” (deadline for confirmation runs out). The app would know how many spots are to be filled, and makes sure that at any time #spots = #confirmed + #invited.

## Views/screens

### Attendee sign-up form

This is the form attendees fill out to register their interest in attending the workshop. The exact details and phrasing of this form changes from workshop to workshop and can be subject to lengthy discussion.

At the last workshop it looked like this:

- First name
- Last name
- E-mail
- Age (above 18 or not)
- With what gender do you identify? (text field)
- Which language do you prefer? (German, English, Either)
- Did you take part in a ClojureBridge workshop before?
- Have you tried any programming before?
- If yes, what sort of things?
- Have you tried programming with Clojure before?
- If we get T-Shirts, what would be your size? (S/M/L/etc + straight/fitted)
- Do you have any food allergies or dietary preferences? (e.g. vegan, vegetarian, GF)    
- Do you need any kind of assistance, or have health issues you would like us to know about?
- Do you need support with child care during the event? (financial or at-the-event care)    
- How did you hear about ClojureBridge Berlin?
- Is there anything else you would like to mention?
- I have read the Code of Conduct, and agree to honor it.
- Do you plan to travel to Berlin from outside Berlin/Brandenburg for the workshop?                                                    

### Coach sign-up form

Similar to the attendee sign-up form, this one is there to allow coaches to register their interest.

- Email Address
- Your first name
- Your last name
- Phone number
- What gender do you identify with? (text field)
- Do you have any food allergies or dietary preferences? (e.g. vegan, vegetarian, GF)
- Do you need any kind of assistance, or have health issues you would like us to know about?
- Which languages can you coach in?
- How much Clojure experience do you have? (from 1 to 5)
- Did you coach before? And if so, where?
- Who would you prefer to coach for?
  \- Coding beginners (ppl who never code before)
  \- Programmers (ppl who know a different language already)
  \- No preference
- Would you be okay with being a floating coach? (yes/no)
- What is your background? Which languages do you know well besides Clojure? (text field)
- Do you need support with child care during the event? (financial or at-the-event care)
- If we get T-Shirts, what would be your size? (including fitted/straight)
- Anything else you like to mention?
- I have read and agree with the Berlin Code of Conduct
- Do you plan to travel to Berlin from outside Berlin/Brandenburg for the workshop?

### List of attendees

Visible to: organizers

Tabular view of key data about attendees (name, language, experience, etc), searchable, and able to be filtered by state (accepted, invited, etc).

### List of coaches

Visible to: organizers

Similar to list of attendees, but with coach data.

### Individual details and history

Visible to: organizers, individual attendee (own application)

It should be possible to pull up an individual, and see all the information they submitted, as well as the history of their application. For example

- Attendee submitted form at $date
- Was randomly selected and invited at $date
- $organizer set their application to “accepted” at $date
- $organizer added a comment
- $attendee was assigned to $group

### Groups

Visible to: organizers (also coaches)?

A dedicated view where all “accepted” attendees and coaches can be divided into groups.

Groups consist of a number of coaches and attendees that will together go through the workshop material. A typical ClojureBridge group consists of two coaches and four attendees, although different constellations occur commonly.

A group may have a number and a name, to give them some personality and a handy mnemonic, e.g. “Amazing Rainbows”.

It can be quite a puzzle to split people into groups. Things that are taken into account

- Language preference (English vs German speaking groups)
- Experience level (put beginners together, assign coaches that enjoy working with beginners)
- Coach pairing: have preferably at least one experienced coach in each group. Try to have at least one woman coach in each group. Take into consideration personal preferences of who wants to coach with whom.
- Prior experience. People who have programmed in a certain language before can be put together, and assigned coaches that have experience with that language.

This is made more difficult by the fact that people will cancel, and new people will be invited, until the very last moment, so groups are continuously reshuffled until the puzzle fits again.

In the attendomat app we tried to visualize some of these aspects with icons (emoji), and this helped.

### Summaries

Visible to: organizers

Certain information about individuals is more usable when summarized across all those who will actually attend, so organizers can see at a glance what to provide for. This includes

- Food preferences
- Accessibility needs
- T-shirt sizes
- Need for child care

## General considerations

### Multiple roles

It is not uncommon for a person to both an organizer and a coach. Across workshops it also occurs that a person is first an attendee, then later a coach, and then an organizer. Ideally the application can support these multiple roles for a single user/login.

### Privacy

At the moment we periodically delete old application data, because we don’t want to hold on to this private and potentially sensitive information longer than necessary. Germany also has quite stringent privacy laws, so we err on the safe side.

The downside of this is that occasionally this historic data would be useful. If a person was rejected at a previous workshop, then we want to make sure they get invited first for the next one.

If users have the possibility to see, update, and delete the data we store about them, then that would enable us to hold on to the data, and use it for these kind of purposes.

### Internationalization

We make an effort to make the workshops accessible to people who speak either German or English. In the google forms version we did that by adding a German translation to each question. Emails contain both an English and a German version of the same text.

## Stretch goals

### Self-service accept/decline

Currently once a person is invited we manually send them an email asking them to confirm or decline. Several people monitor the ClojureBridge Berlin email inbox, and keep the attendee data up to date.

A lot of work, and chances for mistakes, would go away if the email to invitees would go out automatically, and would include two buttons/links, so people can mark themselves as accepted or cancelled.

### Email template integration

Organizing a workshop means a lot of emailing, often sending out specific email templates to a specific cohort, e.g. “all accepted attendees”. You can see some of these email templates in our wiki: [https://github.com/clojurebridge-berlin/organization/wiki/All-email-templates](https://www.google.com/url?q=https://github.com/clojurebridge-berlin/organization/wiki/All-email-templates&sa=D&ust=1518942662085000&usg=AFQjCNEu4klxPJGRudO0xtH1bnxfupox5Q) (links are currently broken, try the links in the sidebar).

It would be useful to be able to create, maintain, test, and use these templates from the app, so that we easily can collaborate on these emails, send them out, and have a log of what was sent to whom.

### Multi-workshop / multi-organization

Workshops happen regularly (in the case CB about twice a year). If the system had a first class concept of a workshop, then people could reuse the same account data to apply again in the future. It would also give the organizers an opportunity to see more details about a person, e.g. to accept those who were rejected before, or to reject those who attended before.

If the app also had a first class concept of an organization, then various ClojureBridge groups could share a single instance, or various groups within the same city (CB, Code_curious, PyLadies, etc.) could share an instance. This would potentially allow registered attendees to sign up to be notified when one of these workshops in their city opens registration.