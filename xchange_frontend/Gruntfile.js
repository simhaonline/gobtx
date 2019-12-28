module.exports = function (grunt) {
    require('load-grunt-tasks')(grunt);
    grunt.config.init({
        aliyunConfig: grunt.file.readJSON('aliyunConfig.json'),
        config: {
            src: 'src',
            dist: 'build',
            version: '0.1.1',
            cdnURL: '<%= aliyunConfig.cdnURL %>/<%= aliyunConfig.dir %>/'
        },
        clean: ['<%= config.dist %>'],
        // 压缩js
        uglify: {
            target: {
                files: [{
                    expand: true,
                    cwd: '<%= config.src %>',
                    src: '**/*.js',
                    dest: '<%= config.dist %>'
                }]
            }
        },
        // 压缩css
        cssmin: {
            target: {
                files: [{
                    expand: true,
                    cwd: '<%= config.src %>',
                    src: '**/*.css',
                    dest: '<%= config.dist %>'
                }]
            }
        },
        // COPY字体文件
        copy: {
            fonts: {
                expand: true,
                cwd: '<%= config.src %>',
                src: ['assets/fonts/***'],
                dest: '<%= config.dist %>',
            },
            tv: {
                expand: true,
                cwd: '<%= config.src %>',
                src: ['assets/tv/**'],
                dest: '<%= config.dist %>',

            },
            images: {
                expand: true,
                cwd: '<%= config.src %>',
                src: ['assets/images/**'],
                dest: '<%= config.dist %>',
            },
            images2: {
                expand: true,
                cwd: '<%= config.src %>',
                src: ['assets/img/**'],
                dest: '<%= config.dist %>',
            },
        },
        // 合并html文件
        processhtml: {
            options: {
                data: {
                    message: 'Hello world!'
                }
            },
            dist: {
                files: [
                    {
                        expand: true,
                        cwd: '<%= config.src %>',
                        src: '*.html',
                        dest: '<%= config.dist %>'
                    }
                ]
            }
        },

        // 压缩html文件
        htmlmin: {
            main: {
                options: {
                    removeComments: true,
                    collapseWhitespace: true
                },
                files: [
                    {
                        expand: true,
                        cwd: '<%= config.dist %>',
                        src: '**/*.html',
                        dest: '<%= config.dist %>'
                    }
                ]
            }
        },
        // 文件md5
        cacheBust: {
            main: {
                options: {
                    assets: ['assets/**'],
                    deleteOriginals: true,
                    baseDir: 'dist/'
                },
                files: [{
                    expand: true,
                    cwd: '<%= config.dist %>',
                    src: ['**/*.html', '**/*.css', '**/*.js'],
                }]
            }
        },
        // 替换cdn的链接
        cdnify: {
            main: {
                options: {
                    base: '<%= config.cdnURL %>'
                },
                files: [{
                    expand: true,
                    cwd: '<%= config.dist %>',
                    src: ['*.html'],
                    dest: '<%= config.dist %>'
                }]
            },
            css: {
                options: {
                    base: '<%= config.cdnURL %>dir/'
                },
                files: [{
                    expand: true,
                    cwd: '<%= config.dist %>',
                    src: ['css/*.css'],
                    dest: '<%= config.dist %>'
                }]
            }
        },
        // 上传到阿里云中
        aliyun_oss: {
            default_options: {
                options: {
                    accessKeyId: '<%= aliyunConfig.accessKeyId %>',
                    secretAccessKey: '<%= aliyunConfig.secretAccessKey %>',
                    endpoint: '<%= aliyunConfig.endpoint %>',
                    bucketName: '<%= aliyunConfig.bucketName %>',
                    cacheControl: 'no-cache'
                },
                files: [
                    {
                        expand: true,
                        cwd: '<%= config.dist %>',
                        src: ['css/**', 'img/**', 'js/**'],
                        dest: '<%= aliyunConfig.dir %>'
                    }
                ]
            }
        }
    });

    grunt.registerTask('build', [
        'clean',
        'processhtml',
        'uglify',
        'cssmin',
        'copy',
        'htmlmin',
        //'cacheBust',
        // 'cdnify',
        // 'aliyun_oss'
    ]);
}
