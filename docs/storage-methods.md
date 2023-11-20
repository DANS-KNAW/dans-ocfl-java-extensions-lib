

# Read-only methods - implemented for both staging dir and archived layers
listDirectory(String directoryPath) -> Layer
listRecursive(String directoryPath) -> Layer
directoryIsEmpty(String directoryPath)
iterateObjects()
fileExists(String filePath)
read(String filePath)
readToString(String filePath)
readLazy(String filePath, DigestAlgorithm algorithm, String digest)
copyDirectoryOutOf(String source, Path destination)

# Write methods - only implemented for staging dir
write(String filePath, byte[] content, String mediaType)
createDirectories(String path)
copyFileInto(Path source, String destination, String mediaType)
copyFileInternal(String sourceFile, String destinationFile)
moveDirectoryInto(Path source, String destination)
moveDirectoryInternal(String source, String destination)

# Delete methods - implemented for both staging dir and archived layers
deleteDirectory(String path)
deleteFile(String path)
deleteFiles(Collection<String> paths)
deleteEmptyDirsDown(String path)
deleteEmptyDirsUp(String path)
