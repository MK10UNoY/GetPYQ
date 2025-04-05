# GetPYQ - Past Year Question Papers Organizer

**GetPYQ** is a robust Android application tailored to streamline the uploading, organization, and retrieval of Past Year Question Papers (PYQs). It harnesses the power of **Jetpack Compose**, **Firebase Authentication**, **Supabase**, and **Cloudinary** to offer a seamless and modern user experience.

---

## Features

### **User Roles**
- **Guest Users**: Browse and download publicly available PDFs.
- **Uploaders**: Authenticated via Firebase to upload PYQs and view their upload history.

### **Document Management**
- Upload PDFs directly from structured folders.
- Efficient cloud storage and retrieval using **Cloudinary**.
- Metadata and upload tracking powered by **Supabase**.

### **Smart Filtering & Search**
- Filter documents by **Semester**, **Subject**, and **Exam Type**.
- View documents within the app.
- Offline access via PDF downloads.

### **Modern UI**
- Intuitive and responsive interface built with **Jetpack Compose**.
- **Modal Drawer** integration for user uploads.
- Optimized performance using **Lazy Lists**.

### **Real-time Sync & Secure Access**
- **Firebase Authentication** ensures secure uploader access.
- **Supabase** for real-time document tracking.
- **Cloudinary** for high-performance PDF hosting.

---

## Tech Stack

| Technology            | Purpose                          |
|----------------------|----------------------------------|
| **Jetpack Compose**   | UI Components & Navigation       |
| **Firebase Auth**     | User Authentication              |
| **Supabase**          | Upload Metadata & Tracking       |
| **Cloudinary**        | File Hosting & PDF Management    |
| **Hilt (DI)**         | Dependency Injection             |
| **Kotlin Coroutines** | Asynchronous Operations          |

---

## Folder Structure

```
getpyq/
 ├── app/         # Main application logic
 ├── ui/          # Jetpack Compose UI components
 ├── viewmodels/  # MVVM architecture ViewModels
 ├── repository/  # Data handling logic
 ├── storage/     # Cloudinary & local storage
 ├── data/        # Data models and schema
 ├── utils/       # Helper functions and utilities
```

---

## Getting Started

### **Step 1: Clone the Repository**
```sh
git clone https://github.com/MK10UNoY/GetPYQ.git
cd GetPYQ
```

### **Step 2: Set Up Firebase**
- Create a Firebase project.
- Enable **Email Authentication**.
- Download `google-services.json` and place it in the `app/` directory.

### **Step 3: Configure Supabase**
- Create a new **Supabase Project**.
- Add a table named `uploadtrackregister` for tracking uploads.
- Securely store your Supabase URL and API key.

### **Step 4: Build & Run**
```sh
./gradlew build
./gradlew installDebug
```

---

## API Usage

### **Fetch Uploaded PDFs**
```kotlin
uploadTrackingViewModel.fetchSubjectPdfUrls(PyqMetaData(
    uploadsubject = "CS101",
    uploadmonth = "March",
    uploadyear = "2025"
))
```

### **Upload a PDF**
```kotlin
FileStorage.uploadToCloudinary2(
    fileUri = fileUri,
    onSuccess = { fileUrl ->
        saveFileMetadata(
            semester = "4",
            subject = "Algorithms",
            name = "CS209_Algo_March.pdf",
            fileUrl = fileUrl,
            uploaderEmail = "uploader@example.com"
        )
    }
)
```

---

## Contributors

**Mrinmoy Koiri**  
- GitHub: [@MK10UNoY](https://github.com/MK10UNoY)  
- LinkedIn: [Mrinmoy Koiri](https://www.linkedin.com/in/mrinmoy-koiri-a1a03327b/)

---

## License

This project is licensed under the **MIT License**. See the [LICENSE](LICENSE) file for more details.

---

## Support & Contributions

- Found a bug? Feel free to [open an issue](https://github.com/MK10UNoY/GetPYQ/issues)
- Want to contribute? Submit a pull request
- Like the project? Star the repository to show your support!

---

**Empowering students with organized academic resources. Happy Learning!**
