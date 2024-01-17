TO DO
=====

Note: the information in this document should be turned into proper documentation in the `docs` directory.

Design
------

This library currently has one purpose, which is to provide support for layered storage of OCFL objects.

### Layered storage

Layered storage is a collection of archive files (for example TAR files), together containing a directory hierarchy.
Each archive file represents a layer. The layers are ordered, with the first layer being the oldest and the last layer
being the newest. In principle the layers are immutable, but in practice they can be updated by adding new layers.

The layered storage can be unpacked into a staging directory by unpacking the layers in order. This means that files in
the newest layer overwrite files with the same path in older layers.

Above description is a simplification. In practice, the layered storage is a bit more complex. A layer can also be
staged, which means that it is unpacked into a staging directory. It is then said to be "open", and it is possible to
add, update and delete files in the staging directory. When a staged layer is closed, mutations are no longer possible.
A staged layer can then be archived, which means that it is packed into an archive file.

The archived and staged layers together form the layered storage.

Note that the layered storage model is independent of the OCFL model. The purpose of this library is to implement
`ocfl-java`'s `Storage` interface, so that an OCFL Object Root can be stored in a layered storage.

#### Deleting files

Deleting files can only be achieved by recreating all the layers that contain the file to be deleted. This is an expensive
operation, especially if the layers are stored on tape. Therefore, this should be avoided as much as possible. __Logically__
deleting files can be achieved at the OCFL level with an OCFL extension which will be described elsewhere.

### Classes

#### `LayeredStorage`

This class implements the `Storage` interface from `ocfl-java`. It is the main entry point for using this library. The contract
is described in the JavaDoc of the `Storage` interface. It is not thread-safe. However, read-only operations can be performed
concurrently by multiple threads. Write operation on different OCFL Objects can also be performed concurrently. Write operations
on the same OCFL Object must be synchronized by the caller.

#### `LayerManager`

This interface is implemented by a class that manages `Layer` objects. It will create `Layer` objects on demand. Since `Layer` objects are lightweight, it is
not necessary to cache them.

#### `Layer`

This interface represents a layer in the layered storage. It provides methods to read and write files in the layer, and to transition
the layer from "open" to "closed", and from "closed" to "archived".

#### `LayerDatabase`

This interface provides access to the layer database. The layer database contains information about the layers and the files and directories
stored in them. `LayeredStorage` uses the layer database as the authoritative source of information about the layered storage. 






