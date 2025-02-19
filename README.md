# EventApp - Event Management Application

## 📌 Table of Contents
- [Walkthrough Video](#walkthrough-video)
- [Overview](#overview)
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Setting up Google Services](#setting-up-google-services)
- [Usage](#usage)
- [Custom Theme & Color Scheme](#custom-theme--color-scheme)
- [Code Structure](#code-structure)
- [Contributing](#contributing)
- [License](#license)
- [Acknowledgments](#acknowledgments)
- [Contact](#contact)

## Walkthrought Video
[Watch on YouTube](https://www.youtube.com/watch?v=JL0cJN1iK9U)

## Overview
EventApp is an Android application designed to help users manage and participate in various events. The app allows users to create, view, and register for events, as well as manage their profiles. It includes features such as event creation, event registration, event filtering, and integration with Google Calendar.

## Features
- **User Authentication**: Users can register and log in using their email or Google account. ![Login Screen](screenshots/login_screen.png)![Google Signin](screenshots/google_signin.png)
- **Event Management**: Staff users can create, edit, and delete events. Regular users can view and register for events. ![Event List](screenshots/Event_List.png)
- **Event Filtering**: Users can filter events by time (today, week, month) and status (not started, ongoing, ended, full). ![Event Filter](screenshots/Event_Filter.png)
- **Event Registration**: Users can register for events and cancel their registration.![My Event Screen](screenshots/My_Event.png)
- **Google Calendar Integration**: Users can add events to their Google Calendar.
- **Profile Management**: Users can update their profile picture and view their registered events.![My Profile](screenshots/Profile_Page.png)
- **Custom Theme and Colors**: Updated the app’s theme with a new color palette, applying a warm and modern beige, caramel, and brown color scheme across all UI elements.

## Technologies Used
- **Android SDK**: The app is built using the Android SDK and follows Material Design guidelines.
- **Material Design Components**: Implemented Material Components for a modern UI.
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

## Setting up Google Services
This project requires a `google-services.json` file for Firebase configuration.

### Steps to generate `google-services.json`:
1. Go to [Firebase Console](https://console.firebase.google.com/).
2. Select the project and go to "Project Settings".
3. In the "General" tab, scroll down to "Your apps".
4. Click on the Android app and download the `google-services.json` file.
5. Place it in the `app/` directory.

⚠️ **For security reasons, this file is not included in the repository.**

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

