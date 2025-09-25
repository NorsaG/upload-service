curl -X PATCH -k http://localhost:8089/api/v1/files/8a94a438-a299-43c8-a1fc-b0b96ad0cee2/visibility \
  -H "Content-Type: application/json" \
  -d '{
        "userId": "2",
        "visibility":  "PUBLIC"
      }'