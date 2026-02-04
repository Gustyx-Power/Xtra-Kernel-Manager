package id.xms.xtrakernelmanager.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import id.xms.xtrakernelmanager.ui.theme.*

/**
 * Responsive container that adapts layout based on screen size
 * - Phone: Single column with full width
 * - Tablet: Multi-column with max width constraint
 */
@Composable
fun ResponsiveContainer(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    val dimens = rememberResponsiveDimens()
    
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = dimens.getMaxContentWidth())
                .fillMaxWidth()
                .padding(horizontal = dimens.screenHorizontalPadding)
        ) {
            content()
        }
    }
}

/**
 * Responsive grid that adapts column count based on screen size
 */
@Composable
fun <T> ResponsiveGrid(
    items: List<T>,
    modifier: Modifier = Modifier,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(16.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    contentPadding: PaddingValues = PaddingValues(0.dp),
    itemContent: @Composable (T) -> Unit
) {
    val dimens = rememberResponsiveDimens()
    
    LazyVerticalGrid(
        columns = GridCells.Fixed(dimens.getGridColumns()),
        modifier = modifier,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        contentPadding = contentPadding
    ) {
        items(items) { item ->
            itemContent(item)
        }
    }
}

/**
 * Responsive row that stacks vertically on phones and horizontally on tablets
 */
@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    verticalAlignment: Alignment.Vertical = Alignment.Top,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    content: @Composable () -> Unit
) {
    val dimens = rememberResponsiveDimens()
    
    if (dimens.isTablet()) {
        Row(
            modifier = modifier.fillMaxWidth(),
            verticalAlignment = verticalAlignment,
            horizontalArrangement = horizontalArrangement
        ) {
            content()
        }
    } else {
        Column(
            modifier = modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            content()
        }
    }
}

/**
 * Adaptive card that adjusts its layout based on screen size
 */
@Composable
fun AdaptiveCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val dimens = rememberResponsiveDimens()
    
    GlassmorphicCard(
        modifier = modifier,
        onClick = onClick,
        contentPadding = PaddingValues(dimens.cardPadding),
        content = content
    )
}