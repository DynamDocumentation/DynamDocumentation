import React from "react";
import { 
  Box, 
  Typography, 
  Paper, 
  Grid, 
  Card, 
  CardContent, 
  CardHeader 
} from "@mui/material";

export default function Dashboard() {
  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h4" gutterBottom>
        Documentation Dashboard
      </Typography>
      
      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Overview
        </Typography>
        <Typography paragraph>
          This dashboard provides quick access to documentation statistics and tools.
          Use the cards below to navigate to different sections of the application.
        </Typography>
      </Paper>
      
      <Grid container spacing={3}>
        <Grid item xs={12} md={6} lg={4}>
          <Card>
            <CardHeader title="Recent Documentation" />
            <CardContent>
              <Typography paragraph>
                View the most recently updated documentation files.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={6} lg={4}>
          <Card>
            <CardHeader title="Popular Sections" />
            <CardContent>
              <Typography paragraph>
                Check out the most frequently accessed documentation sections.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
        
        <Grid item xs={12} md={6} lg={4}>
          <Card>
            <CardHeader title="Documentation Health" />
            <CardContent>
              <Typography paragraph>
                View statistics about documentation coverage and quality.
              </Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>
    </Box>
  );
}
