import React from 'react';
import {
    List, ListItem, ListItemButton, ListItemIcon, ListItemText, Collapse
} from '@mui/material';
import LibraryBooksIcon from '@mui/icons-material/LibraryBooks';
import AddIcon from '@mui/icons-material/Add';
import BugReportIcon from '@mui/icons-material/BugReport';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import ExpandLessIcon from '@mui/icons-material/ExpandLess';

const ToolsMenu = ({ toolsOpen, onToggleTools, onNavigateToLibraryInstaller, onTestButtonClick }) => {
    return (
        <List>
            <ListItem disablePadding>
                <ListItemButton onClick={onToggleTools}>
                    <ListItemIcon>
                        <LibraryBooksIcon />
                    </ListItemIcon>
                    <ListItemText primary="Ferramentas" />
                    {toolsOpen ? <ExpandLessIcon /> : <ExpandMoreIcon />}
                </ListItemButton>
            </ListItem>
            <Collapse in={toolsOpen} timeout="auto" unmountOnExit>
                <List component="div" disablePadding>
                    <ListItemButton 
                        sx={{ pl: 4 }}
                        onClick={onNavigateToLibraryInstaller}
                    >
                        <ListItemIcon>
                            <AddIcon />
                        </ListItemIcon>
                        <ListItemText primary="Instalar Biblioteca" />
                    </ListItemButton>
                    
                    {/* Nova seção de testes */}
                    <ListItemButton 
                        sx={{ pl: 4 }}
                        onClick={() => onTestButtonClick('numpy')}
                    >
                        <ListItemIcon>
                            <BugReportIcon />
                        </ListItemIcon>
                        <ListItemText primary="Teste Numpy" />
                    </ListItemButton>
                    
                    <ListItemButton 
                        sx={{ pl: 4 }}
                        onClick={() => onTestButtonClick('sklearn')}
                    >
                        <ListItemIcon>
                            <BugReportIcon />
                        </ListItemIcon>
                        <ListItemText primary="Teste Sklearn" />
                    </ListItemButton>
                </List>
            </Collapse>
        </List>
    );
};

export default ToolsMenu;