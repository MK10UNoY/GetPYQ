# 📚 GetPYQ - Past Year Question Papers Organizer

GetPYQ is an Android application designed to streamline the process of uploading, organizing, and accessing past year question papers (PYQs) efficiently. The app leverages **Jetpack Compose**, **Firebase Authentication**, **Supabase**, and **Cloudinary** for a seamless experience.

---

## 🚀 Features

### 📌 **User Roles**
- **Guest Users**: Browse and access available PDFs.
- **Uploaders**: Authenticate via Firebase, upload PYQs, and track uploads.

### 📂 **Document Management**
- Upload question papers from a structured folder format.
- Store and manage PDFs efficiently in **Cloudinary**.
- Fetch uploaded documents via **Supabase**.

### 🔍 **Smart Filtering & Search**
- Browse PYQs based on **Semester, Subject, and Exam Type**.
- View uploaded files directly within the app.
- Download PDFs for offline access.

### 🎨 **Modern UI**
- **Jetpack Compose UI** for a dynamic and smooth experience.
- **Modal drawer** to view uploaded PDFs by the current user.
- **Lazy lists** for efficient document browsing.

### 🔄 **Real-time Sync & Security**
- **Firebase Authentication** for uploader verification.
- **Supabase Postgres Database** for tracking uploads.
- **Cloudinary Storage** for optimized file storage.

---

## 🛠️ Tech Stack

| Technology      | Usage                    |
|---------------|--------------------------|
| **Jetpack Compose** | UI Components & Navigation |
| **Firebase Auth** | User Authentication |
| **Supabase** | Database for tracking uploads |
| **Cloudinary** | File Storage & PDF Processing |
| **Hilt (DI)** | Dependency Injection |
| **Kotlin Coroutines** | Async operations |

---

## 📦 Folder Structure

```
📂 getpyq/
 ├── 📂 app/ (Main application logic)
 ├── 📂 ui/ (Jetpack Compose UI screens & components)
 ├── 📂 viewmodels/ (MVVM Architecture ViewModels)
 ├── 📂 repository/ (Data handling logic)
 ├── 📂 storage/ (Cloudinary & local storage handling)
 ├── 📂 data/ (Data classes & models)
 ├── 📂 utils/ (Helper functions & utilities)
```

---

## 🚀 Getting Started

### 1️⃣ **Clone the Repository**
```sh
git clone https://github.com/MK10UNoY/GetPYQ.git
cd GetPYQ
```

### 2️⃣ **Setup Firebase**
- Create a Firebase project.
- Enable **Email Authentication**.
- Download `google-services.json` and place it in `app/`.

### 3️⃣ **Configure Supabase**
- Create a **Supabase Project**.
- Set up the `uploadtrackregister` table.
- Store the Supabase URL and API key securely.

### 4️⃣ **Run the App**
```sh
./gradlew build
./gradlew installDebug
```

---

## 📜 API Usage

### **Fetching Uploaded PDFs**
```kotlin
uploadTrackingViewModel.fetchSubjectPdfUrls(PyqMetaData(
    uploadsubject = "CS101",
    uploadmonth = "March",
    uploadyear = "2025"
))
```

### **Uploading a PDF**
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

## 🛠️ Contributors

👤 **Your Name**  
- GitHub: https://github.com/MK10UNoY/
- LinkedIn: https://www.linkedin.com/in/mrinmoy-koiri-a1a03327b/

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## ⭐ Support & Contributions

- Found a bug? Open an **issue** 🐞.
- Want to contribute? Submit a **pull request** ⚡.
- Love the project? **Star** ⭐ the repo to show your support!

🚀 **Happy Coding!**

