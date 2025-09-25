# upload-service

## Overview
The `upload-service` is a file management service that allows users to upload, download, rename and delete.
Also it gives an ability to manage files with features like visibility control and tag filtering.

## Features
- **File Upload**: Upload files with metadata such as tags, visibility and content type.
- **File Download**: Download files by file id.
- **File Management**: Rename, delete, and change the visibility of files.
- **File Listing**: List files with support for pagination, sorting and filtering by tags or visibility.
- **Tag Management**: Filter files by tags and retrieve distinct tags for user.

## API Endpoints

1. **Upload File**
   - **Endpoint**: `/api/v1/files/upload`
   - **Method**: `POST`
   - **Request Body**:
     ```json
     {
       "userId": "string",
       "fileName": "string",
       "visibility": "PUBLIC | PRIVATE",
       "tags": ["string"],
       "contentType": "string",
       "file": "binary"
     }
     ```
   - **Response**: Returns the metadata of the uploaded file.

2. **Download File**
   - **Endpoint**: `/api/v1/files/{fileId}`
   - **Method**: `GET`
   - **Response**: Returns the file as a binary stream.

3. **Rename File**
   - **Endpoint**: `/api/v1/files/{fileId}/rename`
   - **Method**: `PATCH`
   - **Request Body**:
     ```json
     {
       "newFileName": "string"
     }
     ```
   - **Response**: Returns the updated file metadata.

4. **Delete File**
   - **Endpoint**: `/api/v1/files/{fileId}`
   - **Method**: `DELETE`
   - **Response**: Returns a success message.

5. **Change File Visibility**
   - **Endpoint**: `/api/v1/files/{fileId}/visibility`
   - **Method**: `PATCH`
   - **Request Body**:
     ```json
     {
       "visibility": "PUBLIC | PRIVATE"
     }
     ```
   - **Response**: Returns the updated file metadata.

6. **List Files**
   - **Endpoint**: `/api/v1/files`
   - **Method**: `GET`
   - **Query Parameters**:
     - `userId` (optional): Filter by user ID.
     - `visibility` (optional): Filter by visibility (`PUBLIC` or `PRIVATE`).
     - `tag` (optional): Filter by tag.
     - `page` (optional): Page number for pagination.
     - `size` (optional): Page size for pagination.
     - `sort` (optional): Sorting criteria (e.g., `fileName,asc`).
   - **Response**: Returns a paginated list of files.

7. **List Tags**
   - **Endpoint**: `/api/v1/files/tags`
   - **Method**: `GET`
   - **Query Parameters**:
     - `userId` (required): Filter tags by user ID.
   - **Response**: Returns a list of distinct tags for the user.
 
All real-life examples of curl queries could be found in [Examples](./src/test/resources/requests).


## Requirements
- Java 17 or higher
- Maven 3.8 or higher
- MongoDB 4.4 or higher
