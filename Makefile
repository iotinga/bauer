all:
	./mvnw compile

test:
	./mvnw test

deploy:
	./mvnw deploy
	
release:
	./mvnw versions:set -DnewVersion=$(VERSION)
	./mvnw versions:commit
	git pull 
	git commit -am "bump version to $(VERSION)" || true
	git push
	./mvnw deploy -DskipTests 