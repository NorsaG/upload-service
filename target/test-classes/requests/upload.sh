curl -k http://localhost:8089/api/v1/files/upload \
  -F "file=@C:\Users\norsa\IdeaProjects\upload-service\src\test\resources\requests\upload\test.json" \
  -F "userId=2" \
  -F "fileName=test_3.json" \
  -F "visibility=PRIVATE" \
  -F "tags=json" \
  -F "tags=content" \
  -F "contentType=application/x-json"