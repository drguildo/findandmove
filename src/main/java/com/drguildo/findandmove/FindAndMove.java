package com.drguildo.findandmove;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class FindAndMove extends SimpleFileVisitor<Path> {
  private static Pattern pattern;
  private static Path destDir;
  // print what will happen instead of performing it
  private static boolean test = false;

  public static void main(String[] args) throws IOException {
    String p = null;
    String d = null;

    // TODO: sort out this pig disgusting argument logic
    if (args.length == 0) {
      usage();
    }

    if (args[0].equals("-t")) {
      test = true;
    }

    if (args.length == 1) {
      if (test) {
        usage();
      } else {
        p = ".*" + args[0] + ".*";
        d = args[0];
      }
    }

    if (args.length == 2) {
      if (test) {
        p = ".*" + args[1] + ".*";
        d = args[1];
      } else {
        p = args[0];
        d = args[1];
      }
    }

    if (args.length == 3) {
      if (!test) {
        usage();
      }

      p = args[1];
      d = args[2];
    }

    pattern = Pattern.compile(p);
    destDir = Paths.get(d);

    if (!Files.exists(destDir))
      Files.createDirectories(destDir);

    Path path = FileSystems.getDefault().getPath(".");
    FindAndMove fam = new FindAndMove();

    Files.walkFileTree(path, fam);
  }

  private static void usage() {
    System.err.println("usage findandmove [-t] <pattern> [directory]");
    System.exit(-1);
  }

  @Override
  public FileVisitResult visitFile(Path src, BasicFileAttributes attr) {
    Matcher m = pattern.matcher(src.getFileName().toString());
    if (m.matches()) {
      try {
        Path dest = destDir.resolve(src.getFileName());

        if (!test) {
          System.out.println("Moving " + src + " (" + attr.size() + "bytes)");
          Path moved = Files.move(src, dest);
          System.out.println(" -> " + moved);
        } else {
          System.out.println(src + " would be moved to " + dest);
        }
      } catch (FileAlreadyExistsException existsException) {
        System.err.println("File already exists.");
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    return FileVisitResult.CONTINUE;
  }

  @Override
  public FileVisitResult visitFileFailed(Path file, IOException e) {
    System.err.println(e);

    return FileVisitResult.CONTINUE;
  }
}
