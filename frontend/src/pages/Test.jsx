import React from "react";
import { Stack, Paper, Typography } from "@mui/material"

export default function Test() {

    return (
        <Stack 
            direction="column"
            justifyContent="center"
            spacing={2}
            sx={{
                padding: 4
            }}
        >
            <Typography variant="h5">
                seaborn.light_palette
            </Typography>
            <Typography variant="body1">
            seaborn.light_palette(color, n_colors=6, reverse=False, as_cmap=False, input='rgb')
            </Typography>
            <Stack direction="column" spacing={2} sx={{ paddingX: 2 }}>
            Make a sequential palette that blends from light to color.
<br />
The color parameter can be specified in a number of ways, including all options for defining a color in matplotlib and several additional color spaces that are handled by seaborn. You can also use the database of named colors from the XKCD color survey.
<br />
If you are using a Jupyter notebook, you can also choose this palette interactively with the choose_light_palette() function.
            
                <Stack direction="row" spacing={2}>
                    <Typography variant="body1">
                        Parameters:
                    </Typography>
                    <Typography variant="body1">

    colorbase color for high values

hex code, html color name, or tuple in input space.
n_colorsint, optional

number of colors in the palette
reversebool, optional

if True, reverse the direction of the blend
as_cmapbool, optional

If True, return a matplotlib.colors.ListedColormap.
input('rgb', 'hls', 'husl', 'xkcd')

Color space to interpret the input color. The first three options apply to tuple inputs and the latter applies to string inputs.


                    </Typography>
                </Stack>
            </Stack>
        </Stack>
    )
}