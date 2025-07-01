import React, { useState } from "react";
import { Box } from "@mui/material";
import Header from "../components/documentation/Header";
import DocumentationPanel from "../components/documentation/DocumentationPanel";

export default function Welcome() {
    // State for storing the panels as a horizontal list
    const [panels, setPanels] = useState([
        { id: 1, content: null }
    ]);

    let nextId = 2;

    // Function to add a new panel next to the current panel
    const handleSplit = (panelId) => {
        // Add a new panel to the right of the selected panel
        const panelIndex = panels.findIndex(panel => panel.id === panelId);
        if (panelIndex !== -1) {
            const newPanel = { id: nextId++, content: null };
            const newPanels = [...panels];
            newPanels.splice(panelIndex + 1, 0, newPanel);
            setPanels(newPanels);
        }
    };

    // Function to close/remove a panel
    const handleClose = (panelId) => {
        // Remove the panel from the list
        setPanels(prevPanels => {
            // Don't allow closing if only 1 panel remains
            if (prevPanels.length <= 1) {
                return prevPanels;
            }
            return prevPanels.filter(panel => panel.id !== panelId);
        });
    };
    
    // Function to add content to a panel
    const handleAddContent = (panelId, content) => {
        // Update the content of the specific panel
        setPanels(prevPanels => 
            prevPanels.map(panel => 
                panel.id === panelId 
                    ? { ...panel, content } 
                    : panel
            )
        );
    };

    return (
        <Box 
            sx={{ 
                px: 3, /* Horizontal padding */
                py: 2, /* Vertical padding */
                height: '100%',
                backgroundColor: 'white' 
            }}
        >
            <Header />
            
            <Box 
                sx={{ 
                    height: 'calc(100% - 100px)', 
                    width: '100%', 
                    overflow: 'auto',
                    display: 'flex',
                    flexDirection: 'row',
                    padding: '0.5px', /* 0.5 on each side, matching the card mx of 0.5 */
                    '&::-webkit-scrollbar': {
                        height: '10px'
                    },
                    '&::-webkit-scrollbar-thumb': {
                        backgroundColor: 'rgba(0,0,0,0.2)',
                        borderRadius: '5px'
                    }
                }}
            >
                {panels.map(panel => (
                    <DocumentationPanel 
                        key={panel.id}
                        panel={panel}
                        panels={panels}
                        onSplit={handleSplit}
                        onClose={handleClose}
                        onAddContent={handleAddContent}
                    />
                ))}
            </Box>
        </Box>
    );
}