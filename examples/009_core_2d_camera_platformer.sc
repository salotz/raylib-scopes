#!/usr/bin/env scopes

using import struct
using import Array

let rl = (import ..src.raylib)
using rl.macros

## Screen
let
    SCREEN_WIDTH = 800
    SCREEN_HEIGHT = 450
    FPS = 60
    G = 400
    PLAYER_JUMP_SPEED = 350.0:f32
    PLAYER_HOR_SPEED = 200.0:f32


## Structs
struct Player
    position : rl.Vector2
    speed : f32
    can-jump : bool

struct EnvItem
    rect : rl.Rectangle
    color : rl.Color
    blocking : bool


## Update functions
fn update-player (player env-items delta)

    if (rl.IsKeyDown rl.KEY_LEFT)
        (. player position x) -= (PLAYER_HOR_SPEED * delta)

    if (rl.IsKeyDown rl.KEY_RIGHT)
        (. player position x) += (PLAYER_HOR_SPEED * delta)

    # NOTE: the `deref` is needed because of a bug in the compiler's
    # typechecking, remove when fixed.
    if
        and
            (rl.IsKeyDown rl.KEY_SPACE)
            (deref player.can-jump)

        player.speed = -PLAYER_JUMP_SPEED
        player.can-jump = false


    if
        and
            (rl.IsKeyDown rl.KEY_SPACE)
            (deref player.can-jump)

        player.speed = -PLAYER_JUMP_SPEED
        player.can-jump = false

    local hit-obstacle = false
    for env-item in env-items

        let player-position = player.position


        if
            and
                deref env-item.blocking
                >=
                    (. env-item rect x) + (. env-item rect width)
                    player-position.x
                >=
                    (. env-item rect y)
                    player-position.y
                <
                    (. env-item rect y)
                    player-position.y + (player.speed * delta)

            print "hit obstacle"

            hit-obstacle = 1
            player.speed = 0.0
            player-position.y = (. env-item rect y)

    if (not hit-obstacle)
        (player.position . y) += (player.speed * delta)
        player.speed += (G * delta)
        player.can-jump = false
    else
        player.can-jump = true





## Main

local player =
    Player
        (position = (rl.Vector2 400 280))
        (speed = 0:f32)
        (can-jump = false)

local player_rectangle =
    rl.Rectangle
        (x = (player.position.x - 20))
        (y = (player.position.y - 40))
        (width = 40)
        (height = 40)

local env-items =
    (Array EnvItem)
        EnvItem
            (rect = (rl.Rectangle 0 0 1000 400))
            (color = rl.Colors.LIGHTGRAY)
            (blocking = false)
        EnvItem
            (rl.Rectangle 0 400 1000 200)
            rl.Colors.GRAY
            true
        EnvItem
            (rl.Rectangle 300 200 400 10)
            rl.Colors.GRAY
            true
        EnvItem
            (rl.Rectangle 250 300 100 10)
            rl.Colors.GRAY
            true
        EnvItem
            (rl.Rectangle 650 300 100 10)
            rl.Colors.GRAY
            true

let env-items-len = (countof env-items)


## Camera

# in the original code they used function pointers to dispatch to the
# right function. In Scopes we have something way better than function
# pointers... Scopes.

# create the scope for the camera update functions

# 
fn update-camera-center (camera player env_items env_items_len delta width height)

fn update-camera-center-inside-map (camera player env_items env_items_len delta width height)
    

fn update-camera-even-out-on-landing (camera player env_items env_items_len delta width height)
    

fn update-camera-player-bounds-push (camera player env_items env_items_len delta width height)
    


let camera-update-funcs =
    do
        let
            camera-center = update-camera-center
            camera-center-inside-map = update-camera-center-inside-map
            camera-even-out-on-landing = update-camera-even-out-on-landing
            camera-player-bounds-push = update-camera-player-bounds-push


# the variables that control which camera function to use
local camera-option = 'camera-center

local camera =
    rl.Camera2D
        player.position
        (rl.Vector2 (SCREEN_WIDTH / 2.0) (SCREEN_HEIGHT / 2.0))
        0.0:f32
        1.0:f32

let camera-descriptions =
    (Array string)
        "Follow player center"
        "Follow player center, but clamp to map edges"
        "Follow player center; smoothed"
        "Follow player center horizontally; updateplayer center vertically after landing"
        "Player push camera on getting too close to screen edge"

do-window:
    SCREEN_WIDTH
    SCREEN_HEIGHT
    "raylib [core] Scopes example - core basic window"
    FPS

    local delta-time = ((rl.GetFrameTime) as f32)

    (update-player player env-items delta-time)

    camera.zoom += ((rl.GetMouseWheelMove) * 0.05)

    # update the player glyph
    player_rectangle.x = (player.position.x - 20)
    player_rectangle.x = (player.position.y - 40)

    do-draw:

        rl.ClearBackground rl.Colors.LIGHTGRAY

        # Do 2D drawing
        rl.BeginMode2D camera

        # draw the environment
        for env_item in env-items
            rl.DrawRectangleRec
                env_item.rect
                env_item.color

        # draw the player
        rl.DrawRectangleRec player_rectangle rl.Colors.RED

        rl.EndMode2D;

# don't accidentally try to export something which is unique
;
