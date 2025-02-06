# EventApp - Event Management Application

## Walkthrought Video
https://www.youtube.com/watch?v=JL0cJN1iK9U

## Overview
EventApp is an Android application designed to help users manage and participate in various events. The app allows users to create, view, and register for events, as well as manage their profiles. It includes features such as event creation, event registration, event filtering, and integration with Google Calendar.

## Features
- **User Authentication**: Users can register and log in using their email or Google account.
- **Event Management**: Staff users can create, edit, and delete events. Regular users can view and register for events.
- **Event Filtering**: Users can filter events by time (today, week, month) and status (not started, ongoing, ended, full).
- **Event Registration**: Users can register for events and cancel their registration.
- **Google Calendar Integration**: Users can add events to their Google Calendar.
- **Profile Management**: Users can update their profile picture and view their registered events.

## Technologies Used
- **Android SDK**: The app is built using the Android SDK and follows Material Design guidelines.
- **SQLite Database**: Local data storage for events, users, and registrations.
- **Google Sign-In**: Integration with Google Sign-In for authentication.
- **Google Calendar API**: Integration with Google Calendar to add events.
- **Glide**: Image loading and caching library for profile pictures.
- **RecyclerView**: Efficiently displays lists of events.
- **SharedPreferences**: Stores user session data and preferences.

## Installation
### Clone the Repository:
```sh
git clone https://github.com/SunLawnY/ActivityPlatform.git
```
### Open in Android Studio:
1. Open Android Studio and select **"Open an existing Android Studio project."**
2. Navigate to the cloned repository and select the project.

### Configure Google Sign-In:
1. Go to the **Google Cloud Console**.
2. Create a new project and enable the **Google Sign-In API**.
3. Download the `google-services.json` file and place it in the `app` directory of the project.

### Run the App:
1. Connect an Android device or start an emulator.
2. Click on the **"Run"** button in Android Studio to build and run the app.

## Usage
### Registration and Login:
- New users can register using their email or Google account.
- Existing users can log in using their credentials.

### Creating Events:
- Staff users can create new events by providing details such as title, description, location, start time, end time, and maximum participants.

### Viewing and Filtering Events:
- Users can view a list of events and filter them by time and status.

### Registering for Events:
- Users can register for events and view their registered events in the **"My Events"** section.

### Profile Management:
- Users can update their profile picture and view their profile information.

## Code Structure
### Activities:
- `LoginActivity`: Handles user login and registration.
- `MainActivity`: Displays the list of events and handles navigation.
- `CreateEventActivity`: Allows staff users to create new events.
- `EventDetailActivity`: Displays details of a specific event and allows users to register.
- `EventEditActivity`: Allows staff users to edit existing events.
- `RegisterActivity`: Handles user registration.

### Fragments:
- `ProfileFragment`: Displays and manages user profile information.

### Adapters:
- `EventAdapter`: Manages the display of events in a RecyclerView.

### Database:
- `DatabaseHelper`: Manages SQLite database operations for events, users, and registrations.

### Models:
- `Event`: Represents an event with properties such as title, description, location, start time, end time, and participants.
- `User`: Represents a user with properties such as username, email, password, and staff status.

### Utils:
- `CalendarHelper`: Handles integration with Google Calendar.

## Contributing
Contributions are welcome! Please fork the repository and create a pull request with your changes.

## License
This project is licensed under the **MIT License**. See the `LICENSE` file for details.

## Acknowledgments
- **Google Sign-In**
- **Google Calendar API**
- **Glide**
- **Material Components for Android**

## Contact
For any questions or feedback, please contact **sunnylaw3571@gmail.com**.

