curl -X PATCH -k "http://localhost:8089/api/v1/files/4285b3ce-28d0-431f-812b-314a6e45b7e1/rename" \
  -H "Content-Type: application/json" \
  -d '{
        "userId": "2",
        "newFileName": "test_2_newName.json"
      }'
