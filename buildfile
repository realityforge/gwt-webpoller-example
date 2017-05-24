require 'buildr/git_auto_version'
require 'buildr/gwt'

desc 'A simple application demonstrating the use of the gwt-webpoller library'
define 'gwt-webpoller-example' do
  project.group = 'org.realityforge.gwt.webpoller.example'

  compile.options.source = '1.8'
  compile.options.target = '1.8'
  compile.options.lint = 'all'

  compile.with :gwt_webpoller, :javax_annotation, :javax_javaee, :gwt_user

  gwt_dir = gwt(['org.realityforge.gwt.webpoller.example.Example'],
                :java_args => %w(-Xms512M -Xmx1024M),
                :draft_compile => (ENV['FAST_GWT'] == 'true'),
                :dependencies => [:javax_validation, :javax_validation_sources] + project.compile.dependencies)

  package(:war)

  clean { rm_rf "#{File.dirname(__FILE__)}/artifacts" }

  iml.add_gwt_facet({'org.realityforge.gwt.webpoller.example.Example' => true},
                    :settings => {:compilerMaxHeapSize => '1024'},
                    :gwt_dev_artifact => :gwt_dev)

  # Hacke to remove GWT from path
  webroots = {}
  webroots[_(:source, :main, :webapp)] = '/' if File.exist?(_(:source, :main, :webapp))
  assets.paths.each { |path| webroots[path.to_s] = '/' if path.to_s != gwt_dir.to_s }
  iml.add_web_facet(:webroots => webroots)

  iml.add_jruby_facet

  ipr.add_exploded_war_artifact(project,
                                :build_on_make => true,
                                :enable_gwt => true,
                                :enable_war => true,
                                :dependencies => [project])
end
