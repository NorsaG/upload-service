curl -k http://localhost:8089/api/v1/files/upload \
  -F "file=@/Users/evgeny.lazarev/IdeaProjects/upload-service/src/test/resources/requests/upload/test.json" \
  -F "userId=2" \
  -F "fileName=test.json" \
  -F "visibility=PRIVATE" \
  -F "tags=json" \
  -F "tags=content" \
  -F "contentType=application/x-json"