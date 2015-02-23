def parseFile(self):
    objectDataStream = open(self.objectFile)
    objectData = json.load(objectDataStream)
    objectDataStream.close()
    return objectData