/*
 * Copyright 2015-present Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain
 * a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package com.facebook.buck.rust;

import static org.junit.Assert.assertThat;

import com.facebook.buck.cxx.toolchain.linker.Linker;
import com.facebook.buck.io.file.FileScrubber;
import com.facebook.buck.io.filesystem.ProjectFilesystem;
import com.facebook.buck.model.BuildTarget;
import com.facebook.buck.model.BuildTargetFactory;
import com.facebook.buck.rules.BuildRuleParams;
import com.facebook.buck.rules.BuildRuleResolver;
import com.facebook.buck.rules.DefaultSourcePathResolver;
import com.facebook.buck.rules.FakeSourcePath;
import com.facebook.buck.rules.SourcePath;
import com.facebook.buck.rules.SourcePathResolver;
import com.facebook.buck.rules.SourcePathRuleFinder;
import com.facebook.buck.rules.TestBuildRuleParams;
import com.facebook.buck.rules.TestBuildRuleResolver;
import com.facebook.buck.rules.Tool;
import com.facebook.buck.rules.args.Arg;
import com.facebook.buck.rules.args.StringArg;
import com.facebook.buck.testutil.FakeProjectFilesystem;
import com.facebook.buck.util.HumanReadableException;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSortedSet;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;
import org.hamcrest.Matchers;
import org.junit.Test;

public class RustCompileTest {
  @Test(expected = HumanReadableException.class)
  public void noCrateRootInSrcs() {
    RustCompileRule linkable = FakeRustCompileRule.from("//:donotcare", ImmutableSortedSet.of());
    linkable.getCrateRoot();
  }

  @Test
  public void crateRootMainInSrcs() {
    RustCompileRule linkable =
        FakeRustCompileRule.from(
            "//:donotcare", ImmutableSortedSet.of(FakeSourcePath.of("main.rs")));
    assertThat(linkable.getCrateRoot().toString(), Matchers.endsWith("main.rs"));
  }

  @Test
  public void crateRootTargetNameInSrcs() {
    RustCompileRule linkable =
        FakeRustCompileRule.from(
            "//:myname", ImmutableSortedSet.of(FakeSourcePath.of("myname.rs")));
    assertThat(linkable.getCrateRoot().toString(), Matchers.endsWith("myname.rs"));
  }

  // Test that there's only one valid candidate root source file.
  @Test(expected = HumanReadableException.class)
  public void crateRootMainAndTargetNameInSrcs() {
    RustCompileRule linkable =
        FakeRustCompileRule.from(
            "//:myname",
            ImmutableSortedSet.of(FakeSourcePath.of("main.rs"), FakeSourcePath.of("myname.rs")));
    linkable.getCrateRoot();
  }

  private static Tool fakeTool() {
    return new Tool() {
      @Override
      public ImmutableList<String> getCommandPrefix(SourcePathResolver resolver) {
        return ImmutableList.of();
      }

      @Override
      public ImmutableMap<String, String> getEnvironment(SourcePathResolver resolver) {
        return ImmutableMap.of();
      }
    };
  }

  private static Linker fakeLinker() {
    return new Linker() {
      @Override
      public ImmutableList<FileScrubber> getScrubbers(ImmutableMap<Path, Path> cellRootMap) {
        return null;
      }

      @Override
      public Iterable<Arg> linkWhole(Arg input) {
        return null;
      }

      @Override
      public Iterable<String> soname(String soname) {
        return null;
      }

      @Override
      public Iterable<Arg> fileList(Path fileListPath) {
        return null;
      }

      @Override
      public String origin() {
        return null;
      }

      @Override
      public String libOrigin() {
        return null;
      }

      @Override
      public String searchPathEnvVar() {
        return null;
      }

      @Override
      public String preloadEnvVar() {
        return null;
      }

      @Override
      public Iterable<String> getNoAsNeededSharedLibsFlags() {
        return null;
      }

      @Override
      public Iterable<String> getIgnoreUndefinedSymbolsFlags() {
        return null;
      }

      @Override
      public ImmutableList<Arg> createUndefinedSymbolsLinkerArgs(
          ProjectFilesystem projectFilesystem,
          BuildRuleParams baseParams,
          BuildRuleResolver ruleResolver,
          SourcePathRuleFinder ruleFinder,
          BuildTarget target,
          Iterable<? extends SourcePath> symbolFiles) {
        return null;
      }

      @Override
      public Iterable<Arg> getSharedLibFlag() {
        return null;
      }

      @Override
      public Iterable<String> outputArgs(String path) {
        return null;
      }

      @Override
      public boolean hasFilePathSizeLimitations() {
        return false;
      }

      @Override
      public SharedLibraryLoadingType getSharedLibraryLoadingType() {
        return SharedLibraryLoadingType.RPATH;
      }

      @Override
      public ImmutableList<String> getCommandPrefix(SourcePathResolver resolver) {
        return ImmutableList.of();
      }

      @Override
      public ImmutableMap<String, String> getEnvironment(SourcePathResolver resolver) {
        return ImmutableMap.of();
      }
    };
  }

  static class FakeRustCompileRule extends RustCompileRule {
    private FakeRustCompileRule(
        BuildTarget target, ImmutableSortedSet<SourcePath> srcs, SourcePath rootModule) {
      super(
          target,
          new FakeProjectFilesystem(),
          TestBuildRuleParams.create(),
          String.format("lib%s.rlib", target),
          fakeTool(),
          fakeLinker(),
          Stream.of("--crate-name", target.getShortName(), "--crate-type", "rlib")
              .map(StringArg::of)
              .collect(ImmutableList.toImmutableList()),
          /* depArgs */ ImmutableList.of(),
          /* linkerFlags */
          ImmutableList.of(),
          srcs,
          rootModule,
          true,
          RustBuckConfig.RemapSrcPaths.NO);
    }

    static FakeRustCompileRule from(String target, ImmutableSortedSet<SourcePath> srcs) {
      BuildTarget buildTarget = BuildTargetFactory.newInstance(target);

      SourcePathRuleFinder ruleFinder = new SourcePathRuleFinder(new TestBuildRuleResolver());

      SourcePathResolver pathResolver = DefaultSourcePathResolver.from(ruleFinder);

      Optional<SourcePath> root =
          RustCompileUtils.getCrateRoot(
              pathResolver,
              buildTarget.getShortName(),
              ImmutableSet.of("main.rs", "lib.rs"),
              srcs.stream());

      if (!root.isPresent()) {
        throw new HumanReadableException("No crate root source identified");
      }
      return new FakeRustCompileRule(buildTarget, srcs, root.get());
    }
  }
}
